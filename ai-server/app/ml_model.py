import numpy as np
import xgboost as xgb
import shap
from sklearn.preprocessing import LabelEncoder

# ────────────────────────────────────────────────────────────────
# 페르소나 코드 정의
#
# Spring Boot의 PersonaCode enum과 반드시 동일한 값을 유지해야 함
# 순서가 바뀌면 LabelEncoder 인코딩이 달라져서 예측 결과가 틀려짐
# ────────────────────────────────────────────────────────────────
PERSONA_CODES = [
    "SAFETY_GUARD",       # 안정 추구형: 원금 보장 최우선, 저위험 선호
    "STEADY_WORKER",      # 꾸준한 저축형: 정기적 적립, 안정적 수익
    "BALANCED_SPENDER",   # 균형 소비형: 소비와 저축의 균형
    "RATE_OPTIMIZER",     # 금리 최적화형: 최고 금리 상품 추구
    "GOAL_ACHIEVER",      # 목표 달성형: 단기 집중 저축으로 목표 달성
    "FUTURE_PLANNER",     # 미래 설계형: 장기 복리 효과 극대화
]

# ────────────────────────────────────────────────────────────────
# XGBoost 모델 초기화
#
# 현재는 데모용 규칙 기반 샘플 데이터로 모델을 즉시 학습시킴
# 실제 서비스 전환 시 아래 순서로 교체:
#   1. 실제 사용자 설문 데이터 수집
#   2. 데이터 전처리 및 레이블링
#   3. 모델 학습 후 model.save_model('model.json')으로 저장
#   4. _build_demo_model() 대신 xgb.XGBClassifier().load_model('model.json') 사용
# ────────────────────────────────────────────────────────────────

def _build_demo_model():
    """
    데모용 XGBoost 분류 모델 생성 및 학습

    입력 특성(feature) 5가지:
      - age          : 나이 (세)
      - income       : 월 소득 (만원)
      - savings_rate : 저축 성향 점수 (0~100, 설문 응답에서 계산)
      - risk_score   : 위험 선호도 점수 (0~100, 설문 응답에서 계산)
      - goal_term    : 목표 기간 (개월)

    출력:
      - model     : 학습된 XGBClassifier
      - explainer : SHAP TreeExplainer (예측 근거 설명용)
      - le        : LabelEncoder (페르소나 코드 ↔ 숫자 변환용)
    """

    # ── 샘플 학습 데이터 ─────────────────────────────────────────
    # 각 행: [age, income, savings_rate, risk_score, goal_term]
    # 페르소나별 3개 샘플 → 총 18개 샘플
    # 실제 서비스에서는 수백~수천 개의 실제 데이터로 교체 필요
    X = np.array([
        # SAFETY_GUARD: 고령, 저소득, 고저축 성향, 매우 저위험, 장기
        [55, 250, 80, 10, 36],
        [60, 200, 90, 5,  48],
        [50, 300, 75, 15, 24],

        # STEADY_WORKER: 중년, 중소득, 중저축 성향, 저위험, 중기
        [35, 300, 60, 30, 12],
        [40, 350, 65, 25, 24],
        [38, 280, 55, 20, 18],

        # BALANCED_SPENDER: 30대 초반, 중소득, 중저축 성향, 중위험, 단중기
        [30, 350, 40, 50, 12],
        [32, 400, 45, 45,  6],
        [28, 320, 35, 55, 12],

        # RATE_OPTIMIZER: 젊은, 고소득, 저저축 성향, 고위험, 단기
        [25, 500, 20, 80, 6],
        [27, 450, 25, 75, 3],
        [26, 480, 15, 85, 6],

        # GOAL_ACHIEVER: 20~30대, 중소득, 고저축 성향, 중위험, 단기
        [24, 250, 70, 40,  6],
        [26, 300, 75, 35, 12],
        [28, 280, 80, 30,  6],

        # FUTURE_PLANNER: 젊은, 중소득, 중저축 성향, 중위험, 초장기
        [22, 200, 50, 40, 60],
        [24, 250, 55, 35, 48],
        [23, 220, 45, 45, 60],
    ])

    # ── 레이블 인코딩 ────────────────────────────────────────────
    # XGBoost는 문자열 레이블을 직접 받지 못하므로 숫자로 변환
    # 예: "SAFETY_GUARD" → 4, "STEADY_WORKER" → 5 (알파벳 순)
    # le.inverse_transform()으로 다시 문자열로 복원 가능
    le = LabelEncoder()
    le.fit(PERSONA_CODES)
    y = le.transform([
        "SAFETY_GUARD",    "SAFETY_GUARD",    "SAFETY_GUARD",
        "STEADY_WORKER",   "STEADY_WORKER",   "STEADY_WORKER",
        "BALANCED_SPENDER","BALANCED_SPENDER","BALANCED_SPENDER",
        "RATE_OPTIMIZER",  "RATE_OPTIMIZER",  "RATE_OPTIMIZER",
        "GOAL_ACHIEVER",   "GOAL_ACHIEVER",   "GOAL_ACHIEVER",
        "FUTURE_PLANNER",  "FUTURE_PLANNER",  "FUTURE_PLANNER",
    ])

    # ── XGBoost 모델 학습 ────────────────────────────────────────
    # n_estimators  : 트리 개수 (많을수록 정확하지만 느림)
    # max_depth     : 트리 최대 깊이 (깊을수록 복잡한 패턴 학습, 과적합 위험)
    # learning_rate : 각 트리의 기여도 (낮을수록 안정적이지만 많은 트리 필요)
    # eval_metric   : 다중 분류 손실함수 (mlogloss = multiclass log loss)
    model = xgb.XGBClassifier(
        n_estimators=100,
        max_depth=4,
        learning_rate=0.1,
        eval_metric="mlogloss",
        random_state=42,  # 재현성을 위한 랜덤 시드 고정
    )
    model.fit(X, y)

    # ── SHAP Explainer 초기화 ────────────────────────────────────
    # TreeExplainer: 트리 기반 모델(XGBoost, LightGBM 등)에 최적화된 SHAP
    # 각 특성이 예측에 얼마나 기여했는지 수치로 설명
    # 예: age의 SHAP값이 +0.5이면 "나이가 이 페르소나 예측에 긍정적으로 기여"
    explainer = shap.TreeExplainer(model)

    return model, explainer, le


# ── 앱 시작 시 모델 1회 로드 (모듈 레벨 싱글톤) ───────────────────
# FastAPI 앱이 시작될 때 한 번만 실행됨
# 요청마다 모델을 새로 만들면 매우 느려지므로 전역으로 유지
_model, _explainer, _label_encoder = _build_demo_model()

# 입력 특성 이름 목록 (SHAP 결과 딕셔너리 키로 사용)
FEATURE_NAMES = ["age", "income", "savings_rate", "risk_score", "goal_term"]


def predict_persona(age: int, income: int, savings_rate: int,
                    risk_score: int, goal_term: int) -> dict:
    """
    XGBoost 페르소나 분류 + SHAP 설명 생성

    처리 흐름:
      1. 입력값을 numpy 배열로 변환
      2. XGBoost로 페르소나 분류 (6개 중 1개)
      3. predict_proba로 예측 확률(confidence) 계산
      4. SHAP TreeExplainer로 각 특성의 기여도 계산
      5. 결과를 딕셔너리로 반환 → FastAPI가 JSON으로 직렬화

    Args:
        age          : 나이
        income       : 월 소득 (만원)
        savings_rate : 저축 성향 점수 (0~100)
        risk_score   : 위험 선호도 점수 (0~100)
        goal_term    : 목표 기간 (개월)

    Returns:
        {
            "persona_code"    : "SAFETY_GUARD",  # 예측된 페르소나
            "confidence"      : 0.87,             # 예측 확률
            "shap_explanation": {                 # 각 특성의 기여도
                "age"         : 0.12,
                "income"      : -0.05,
                "savings_rate": 0.43,
                "risk_score"  : -0.31,
                "goal_term"   : 0.08
            }
        }
    """
    # 2D 배열로 변환 (XGBoost는 (n_samples, n_features) 형태 요구)
    X_input = np.array([[age, income, savings_rate, risk_score, goal_term]])

    # 페르소나 예측
    pred_idx = _model.predict(X_input)[0]
    pred_proba = _model.predict_proba(X_input)[0]
    persona_code = _label_encoder.inverse_transform([pred_idx])[0]
    confidence = float(pred_proba[pred_idx])

    # SHAP 값 계산
    # shap_values: 다중 클래스의 경우 클래스 수만큼의 리스트 반환
    shap_values = _explainer.shap_values(X_input)

    # 예측된 클래스에 해당하는 SHAP 값만 추출
    if isinstance(shap_values, list):
        # 구버전 SHAP: [n_classes, n_samples, n_features] 형태
        shap_for_pred = shap_values[pred_idx][0]
    else:
        # 신버전 SHAP: [n_samples, n_features, n_classes] 형태
        shap_for_pred = shap_values[0, :, pred_idx]

    # 특성 이름과 SHAP 값을 딕셔너리로 조합
    shap_explanation = {
        FEATURE_NAMES[i]: round(float(shap_for_pred[i]), 4)
        for i in range(len(FEATURE_NAMES))
    }

    return {
        "persona_code": persona_code,
        "confidence": round(confidence, 4),
        "shap_explanation": shap_explanation,
    }