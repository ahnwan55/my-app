import { useState } from "react";
import { useNavigate } from "react-router-dom";

// 도서관 샘플 데이터 (실제 구현 시 도서관 정보나루 API로 교체)
const LIBRARIES = [
    { name: "마포구립 서강도서관", region: "서울", city: "마포구" },
    { name: "마포구립 망원도서관", region: "서울", city: "마포구" },
    { name: "종로도서관", region: "서울", city: "종로구" },
    { name: "중구도서관", region: "서울", city: "중구" },
    { name: "용산구립 도서관", region: "서울", city: "용산구" },
    { name: "성동구립 도서관", region: "서울", city: "성동구" },
    { name: "강남구립 논현도서관", region: "서울", city: "강남구" },
    { name: "강남구립 역삼도서관", region: "서울", city: "강남구" },
    { name: "송파구립 도서관", region: "서울", city: "송파구" },
    { name: "노원구립 도서관", region: "서울", city: "노원구" },
    { name: "수원시립 중앙도서관", region: "경기", city: "수원" },
    { name: "성남시립 중앙도서관", region: "경기", city: "성남" },
    { name: "고양시립 도서관", region: "경기", city: "고양" },
    { name: "부천시립 도서관", region: "경기", city: "부천" },
    { name: "용인시립 도서관", region: "경기", city: "용인" },
    { name: "인천광역시 미추홀도서관", region: "인천", city: "미추홀구" },
    { name: "인천시립 연수도서관", region: "인천", city: "연수구" },
    { name: "인천 부평구립 도서관", region: "인천", city: "부평구" },
    { name: "부산시립 시민도서관", region: "부산", city: "부산진구" },
    { name: "부산 해운대구립 도서관", region: "부산", city: "해운대구" },
    { name: "대구시립 중앙도서관", region: "대구", city: "중구" },
    { name: "대전시립 한밭도서관", region: "대전", city: "서구" },
    { name: "광주시립 도서관", region: "광주", city: "북구" },
];

const REGIONS = ["전체", "서울", "경기", "인천", "부산", "대구", "대전", "광주"];

// 중복 닉네임 목록 (실제 구현 시 백엔드 API로 교체)
const TAKEN_NICKNAMES = ["독서왕", "책벌레", "DIVER"];

export default function UserInfoPage() {
    const navigate = useNavigate();

    // 닉네임 관련 상태
    const [nickname, setNickname] = useState("");
    const [nicknameStatus, setNicknameStatus] = useState("idle"); // idle | checking | available | taken
    const [nicknameVerified, setNicknameVerified] = useState(false);

    // 도서관 관련 상태
    const [selectedRegion, setSelectedRegion] = useState("전체");
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedLibrary, setSelectedLibrary] = useState(null);

    // 닉네임 입력 시 검증 상태 초기화
    const handleNicknameChange = (e) => {
        setNickname(e.target.value);
        setNicknameVerified(false);
        setNicknameStatus("idle");
    };

    // 중복 확인 버튼 클릭 시 처리
    // 실제 구현 시 백엔드 API 호출로 교체: GET /api/users/check-nickname?nickname=xxx
    const handleCheckNickname = () => {
        if (nickname.length < 2) return;
        setNicknameStatus("checking");

        // 임시 딜레이 (실제 API 호출 시뮬레이션)
        setTimeout(() => {
            if (TAKEN_NICKNAMES.includes(nickname)) {
                setNicknameStatus("taken");
                setNicknameVerified(false);
            } else {
                setNicknameStatus("available");
                setNicknameVerified(true);
            }
        }, 800);
    };

    // 도서관 필터링 (지역 탭 + 검색어 조합)
    const filteredLibraries = LIBRARIES.filter((lib) => {
        const regionMatch = selectedRegion === "전체" || lib.region === selectedRegion;
        const searchMatch =
            !searchQuery ||
            lib.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            lib.city.toLowerCase().includes(searchQuery.toLowerCase());
        return regionMatch && searchMatch;
    });

    // 다음 버튼 활성화 조건: 닉네임 중복 확인 완료 + 도서관 선택
    const canProceed = nicknameVerified && selectedLibrary;

    // 다음 버튼 클릭 시 처리
    // 실제 구현 시 백엔드 API 호출: POST /api/users/info
    const handleNext = () => {
        if (!canProceed) return;
        console.log("사용자 정보:", { nickname, library: selectedLibrary });
        // TODO: 백엔드 API로 닉네임 + 도서관 정보 저장
        navigate("/");
    };

    return (
        <div style={styles.container}>
            <div style={styles.bgTexture} />
            <div style={styles.inner}>

                {/* 스텝 인디케이터 */}
                <div style={styles.header}>
                    <div style={styles.stepIndicator}>
                        <div style={{ ...styles.step, background: "#C8862A" }} />
                        <div style={{ ...styles.step, background: "#1C2B4A" }} />
                        <div style={styles.step} />
                    </div>
                    <div style={styles.headerLabel}>추가 정보 입력</div>
                    <h1 style={styles.headerTitle}>
                        서비스 이용을 위해<br />정보를 입력해주세요
                    </h1>
                    <p style={styles.headerDesc}>닉네임과 주로 이용하는 도서관을 선택해주세요</p>
                </div>

                {/* 닉네임 카드 */}
                <div style={styles.card}>
                    <div style={styles.cardTitle}>닉네임</div>
                    <div style={styles.nicknameRow}>
                        <input
                            style={{
                                ...styles.input,
                                borderColor: nicknameStatus === "available"
                                    ? "#2E7D52"
                                    : nicknameStatus === "taken"
                                        ? "#C0392B"
                                        : "rgba(28,43,74,0.1)",
                            }}
                            type="text"
                            placeholder="닉네임 입력 (2~10자)"
                            value={nickname}
                            onChange={handleNicknameChange}
                            maxLength={10}
                        />
                        <button
                            style={{
                                ...styles.checkBtn,
                                background: nickname.length >= 2 && nicknameStatus !== "checking"
                                    ? "#1C2B4A"
                                    : "#EDE6D6",
                                color: nickname.length >= 2 && nicknameStatus !== "checking"
                                    ? "#fff"
                                    : "#8A95A8",
                                cursor: nickname.length >= 2 && nicknameStatus !== "checking"
                                    ? "pointer"
                                    : "not-allowed",
                            }}
                            onClick={handleCheckNickname}
                            disabled={nickname.length < 2 || nicknameStatus === "checking" || nicknameVerified}
                        >
                            {nicknameStatus === "checking"
                                ? "확인 중..."
                                : nicknameVerified
                                    ? "확인 완료"
                                    : "중복 확인"}
                        </button>
                    </div>

                    {/* 닉네임 상태 메시지 */}
                    <div style={{
                        ...styles.nicknameHint,
                        color: nicknameStatus === "available"
                            ? "#2E7D52"
                            : nicknameStatus === "taken"
                                ? "#C0392B"
                                : "#8A95A8",
                    }}>
                        {nicknameStatus === "idle" && "2~10자 이내로 입력해주세요"}
                        {nicknameStatus === "checking" && "확인 중..."}
                        {nicknameStatus === "available" && "사용 가능한 닉네임입니다 ✓"}
                        {nicknameStatus === "taken" && "이미 사용 중인 닉네임입니다"}
                    </div>
                </div>

                {/* 도서관 선택 카드 */}
                <div style={styles.card}>
                    <div style={styles.cardTitle}>주 이용 도서관</div>

                    {/* 지역 탭 */}
                    <div style={styles.regionTabs}>
                        {REGIONS.map((region) => (
                            <button
                                key={region}
                                style={{
                                    ...styles.regionTab,
                                    background: selectedRegion === region ? "#1C2B4A" : "#fff",
                                    color: selectedRegion === region ? "#fff" : "#5A6478",
                                    borderColor: selectedRegion === region
                                        ? "#1C2B4A"
                                        : "rgba(28,43,74,0.1)",
                                }}
                                onClick={() => {
                                    setSelectedRegion(region);
                                    setSearchQuery("");
                                }}
                            >
                                {region}
                            </button>
                        ))}
                    </div>

                    {/* 검색창 */}
                    <div style={styles.searchWrap}>
                        <span style={styles.searchIcon}>🔍</span>
                        <input
                            style={styles.searchInput}
                            type="text"
                            placeholder="도서관 이름으로 검색"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                    </div>

                    {/* 도서관 목록 */}
                    <div style={styles.libraryList}>
                        {filteredLibraries.length === 0 ? (
                            <div style={styles.emptyMsg}>검색 결과가 없습니다</div>
                        ) : (
                            filteredLibraries.map((lib) => (
                                <div
                                    key={lib.name}
                                    style={{
                                        ...styles.libraryItem,
                                        borderColor: selectedLibrary === lib.name
                                            ? "#1C2B4A"
                                            : "transparent",
                                        background: selectedLibrary === lib.name ? "#fff" : "#F5F0E8",
                                    }}
                                    onClick={() => setSelectedLibrary(lib.name)}
                                >
                                    <div>
                                        <div style={styles.libraryName}>{lib.name}</div>
                                        <div style={styles.libraryRegion}>{lib.region} · {lib.city}</div>
                                    </div>
                                    <div style={{
                                        ...styles.libraryCheck,
                                        background: selectedLibrary === lib.name ? "#1C2B4A" : "transparent",
                                        borderColor: selectedLibrary === lib.name
                                            ? "#1C2B4A"
                                            : "rgba(28,43,74,0.1)",
                                        color: "#fff",
                                    }}>
                                        {selectedLibrary === lib.name ? "✓" : ""}
                                    </div>
                                </div>
                            ))
                        )}
                    </div>

                    {/* 선택된 도서관 뱃지 */}
                    {selectedLibrary && (
                        <div style={styles.selectedBadge}>
                            <span>{selectedLibrary}</span>
                            <span
                                style={styles.removeBadge}
                                onClick={() => setSelectedLibrary(null)}
                            >
                ×
              </span>
                        </div>
                    )}
                </div>

                {/* 다음 버튼 */}
                <button
                    style={{
                        ...styles.nextBtn,
                        background: canProceed ? "#1C2B4A" : "#EDE6D6",
                        color: canProceed ? "#fff" : "#8A95A8",
                        cursor: canProceed ? "pointer" : "not-allowed",
                    }}
                    onClick={handleNext}
                    disabled={!canProceed}
                >
                    다음으로
                </button>

            </div>
        </div>
    );
}

const styles = {
    container: {
        minHeight: "100vh",
        background: "#F5F0E8",
        position: "relative",
    },
    bgTexture: {
        position: "fixed",
        inset: 0,
        background: `
      radial-gradient(ellipse at 15% 30%, rgba(200,134,42,0.08) 0%, transparent 50%),
      radial-gradient(ellipse at 85% 70%, rgba(28,43,74,0.06) 0%, transparent 50%)
    `,
        pointerEvents: "none",
        zIndex: 0,
    },
    inner: {
        position: "relative",
        zIndex: 1,
        maxWidth: "520px",
        margin: "0 auto",
        padding: "40px 24px 60px",
    },
    header: {
        textAlign: "center",
        marginBottom: "32px",
    },
    stepIndicator: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        gap: "8px",
        marginBottom: "16px",
    },
    step: {
        width: "28px",
        height: "4px",
        borderRadius: "2px",
        background: "#EDE6D6",
    },
    headerLabel: {
        fontSize: "11px",
        letterSpacing: "0.2em",
        color: "#C8862A",
        textTransform: "uppercase",
        fontWeight: "500",
        marginBottom: "10px",
    },
    headerTitle: {
        fontFamily: "'Noto Serif KR', serif",
        fontSize: "22px",
        fontWeight: "700",
        color: "#1C2B4A",
        lineHeight: "1.4",
    },
    headerDesc: {
        fontSize: "13px",
        color: "#5A6478",
        marginTop: "6px",
        fontWeight: "300",
    },
    card: {
        background: "rgba(255,255,255,0.75)",
        backdropFilter: "blur(16px)",
        border: "1px solid rgba(28,43,74,0.1)",
        borderRadius: "24px",
        padding: "32px",
        marginBottom: "16px",
    },
    cardTitle: {
        fontFamily: "'Noto Serif KR', serif",
        fontSize: "14px",
        fontWeight: "600",
        color: "#1C2B4A",
        marginBottom: "16px",
        display: "flex",
        alignItems: "center",
        gap: "8px",
    },
    nicknameRow: {
        display: "flex",
        gap: "8px",
        marginBottom: "6px",
    },
    input: {
        flex: 1,
        padding: "13px 16px",
        background: "#fff",
        border: "1.5px solid rgba(28,43,74,0.1)",
        borderRadius: "12px",
        fontSize: "14px",
        fontFamily: "'Pretendard', sans-serif",
        color: "#1C2B4A",
        outline: "none",
    },
    checkBtn: {
        padding: "13px 16px",
        borderRadius: "12px",
        fontSize: "13px",
        fontWeight: "600",
        border: "none",
        fontFamily: "'Pretendard', sans-serif",
        whiteSpace: "nowrap",
    },
    nicknameHint: {
        fontSize: "12px",
        marginTop: "4px",
        height: "16px",
    },
    regionTabs: {
        display: "flex",
        flexWrap: "wrap",
        gap: "6px",
        marginBottom: "14px",
    },
    regionTab: {
        padding: "6px 14px",
        borderRadius: "20px",
        fontSize: "12px",
        fontWeight: "500",
        cursor: "pointer",
        border: "1.5px solid",
        fontFamily: "'Pretendard', sans-serif",
        transition: "all 0.15s",
    },
    searchWrap: {
        position: "relative",
        marginBottom: "12px",
    },
    searchIcon: {
        position: "absolute",
        left: "14px",
        top: "50%",
        transform: "translateY(-50%)",
        fontSize: "14px",
        pointerEvents: "none",
    },
    searchInput: {
        width: "100%",
        padding: "11px 16px 11px 38px",
        background: "#fff",
        border: "1.5px solid rgba(28,43,74,0.1)",
        borderRadius: "12px",
        fontSize: "13px",
        fontFamily: "'Pretendard', sans-serif",
        color: "#1C2B4A",
        outline: "none",
        boxSizing: "border-box",
    },
    libraryList: {
        maxHeight: "200px",
        overflowY: "auto",
        display: "flex",
        flexDirection: "column",
        gap: "6px",
    },
    libraryItem: {
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "12px 14px",
        border: "1.5px solid transparent",
        borderRadius: "10px",
        cursor: "pointer",
    },
    libraryName: {
        fontSize: "13px",
        fontWeight: "500",
        color: "#1C2B4A",
    },
    libraryRegion: {
        fontSize: "11px",
        color: "#8A95A8",
        marginTop: "2px",
    },
    libraryCheck: {
        width: "18px",
        height: "18px",
        borderRadius: "50%",
        border: "1.5px solid",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: "10px",
        flexShrink: 0,
    },
    selectedBadge: {
        display: "inline-flex",
        alignItems: "center",
        gap: "6px",
        background: "rgba(28,43,74,0.08)",
        border: "1px solid rgba(28,43,74,0.15)",
        borderRadius: "20px",
        padding: "5px 12px",
        fontSize: "12px",
        color: "#1C2B4A",
        fontWeight: "500",
        marginTop: "10px",
    },
    removeBadge: {
        cursor: "pointer",
        color: "#8A95A8",
        fontSize: "14px",
        lineHeight: "1",
    },
    emptyMsg: {
        textAlign: "center",
        padding: "20px",
        fontSize: "13px",
        color: "#8A95A8",
    },
    nextBtn: {
        width: "100%",
        padding: "16px",
        borderRadius: "14px",
        fontSize: "14px",
        fontWeight: "600",
        border: "none",
        fontFamily: "'Pretendard', sans-serif",
    },
};