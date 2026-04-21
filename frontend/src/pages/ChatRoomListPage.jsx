import { useNavigate } from "react-router-dom";

// 채팅방 샘플 데이터 (실제 구현 시 백엔드 API로 교체)
// GET /api/chat/rooms?libraryCode=xxx
const CHAT_ROOMS = [
    {
        personaId: "DIVER",
        icon: "🌊",
        name: "서강도서관 DIVER 방",
        library: "마포구립 서강도서관",
        lastMessage: "책방지기: \"사피엔스 읽으신 분 계세요? 같이 얘기 나눠요 📚\"",
        unread: 3,
        online: 7,
        updatedAt: "방금 전",
        isMine: true, // 내 페르소나 채팅방
        iconBg: "rgba(28,43,74,0.15)",
    },
    {
        personaId: "EXPLORER",
        icon: "🔭",
        name: "서강도서관 EXPLORER 방",
        library: "마포구립 서강도서관",
        lastMessage: "독서왕: \"요즘 과학책에 빠졌어요. 추천해주세요!\"",
        unread: 0,
        online: 12,
        updatedAt: "2분 전",
        isMine: false,
        iconBg: "rgba(55,138,221,0.12)",
    },
    {
        personaId: "CURATOR",
        icon: "🌸",
        name: "서강도서관 CURATOR 방",
        library: "마포구립 서강도서관",
        lastMessage: "책마니아: \"이번 달 에세이 추천 목록 공유합니다 🌿\"",
        unread: 0,
        online: 8,
        updatedAt: "15분 전",
        isMine: false,
        iconBg: "rgba(212,83,126,0.12)",
    },
    {
        personaId: "NAVIGATOR",
        icon: "⚓",
        name: "서강도서관 NAVIGATOR 방",
        library: "마포구립 서강도서관",
        lastMessage: "실용독서: \"자기계발서 읽고 실천한 것들 나눠봐요\"",
        unread: 0,
        online: 5,
        updatedAt: "1시간 전",
        isMine: false,
        iconBg: "rgba(15,110,86,0.12)",
    },
    {
        personaId: "DWELLER",
        icon: "☕",
        name: "서강도서관 DWELLER 방",
        library: "마포구립 서강도서관",
        lastMessage: "쉼독자: \"오늘도 따뜻한 책 한 권 읽었어요 🍵\"",
        unread: 0,
        online: 9,
        updatedAt: "2시간 전",
        isMine: false,
        iconBg: "rgba(200,134,42,0.12)",
    },
    {
        personaId: "ANALYST",
        icon: "♟️",
        name: "서강도서관 ANALYST 방",
        library: "마포구립 서강도서관",
        lastMessage: "비판독자: \"이 책의 논리 구조에 문제가 있어요\"",
        unread: 0,
        online: 6,
        updatedAt: "3시간 전",
        isMine: false,
        iconBg: "rgba(88,88,90,0.12)",
    },
];

export default function ChatRoomListPage() {
    const navigate = useNavigate();

    // 내 채팅방과 다른 채팅방 분리
    const myRoom = CHAT_ROOMS.find((r) => r.isMine);
    const otherRooms = CHAT_ROOMS.filter((r) => !r.isMine);

    return (
        <div style={styles.container}>
            <div style={styles.bgTexture} />

            {/* 네비게이션 */}
            <nav style={styles.navbar}>
                <button style={styles.navBack} onClick={() => navigate("/")}>←</button>
                <div style={styles.navTitle}>페르소나 채팅방</div>
            </nav>

            <div style={styles.inner}>

                {/* 헤더 */}
                <div style={styles.header}>
                    <div style={styles.headerLabel}>독서 커뮤니티</div>
                    <h1 style={styles.headerTitle}>비슷한 독자들과<br />대화해보세요</h1>
                    <p style={styles.headerDesc}>페르소나와 주 이용 도서관이 같은 사람들의 공간입니다</p>
                </div>

                {/* 내 채팅방 */}
                {myRoom && (
                    <div
                        style={styles.myRoomCard}
                        onClick={() => navigate(`/chat/${myRoom.personaId}`)}
                    >
                        <div style={styles.myRoomTag}>✦ 나의 채팅방</div>
                        <div style={styles.myRoomTop}>
                            <div style={styles.myRoomIcon}>{myRoom.icon}</div>
                            <div>
                                <div style={styles.myRoomName}>{myRoom.name}</div>
                                <div style={styles.myRoomSub}>{myRoom.library} · {myRoom.personaId} 페르소나</div>
                            </div>
                        </div>
                        <div style={styles.myRoomBottom}>
                            <div style={styles.myRoomLast}>{myRoom.lastMessage}</div>
                            {myRoom.unread > 0 && (
                                <div style={styles.unreadBadge}>{myRoom.unread} NEW</div>
                            )}
                        </div>
                    </div>
                )}

                {/* 다른 채팅방 목록 */}
                <div style={styles.sectionLabel}>다른 페르소나 채팅방 둘러보기</div>
                <div style={styles.roomList}>
                    {otherRooms.map((room) => (
                        <div
                            key={room.personaId}
                            style={styles.roomCard}
                            onClick={() => navigate(`/chat/${room.personaId}`)}
                        >
                            {/* 페르소나 아이콘 */}
                            <div style={{ ...styles.roomIcon, background: room.iconBg }}>
                                {room.icon}
                            </div>

                            {/* 방 정보 */}
                            <div style={styles.roomInfo}>
                                <div style={styles.roomName}>{room.name}</div>
                                <div style={styles.roomPreview}>{room.lastMessage}</div>
                            </div>

                            {/* 우측 정보 */}
                            <div style={styles.roomRight}>
                                <div style={styles.roomTime}>{room.updatedAt}</div>
                                <div style={styles.roomOnline}>
                                    <div style={styles.onlineDot} />
                                    {room.online}명
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

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
      radial-gradient(ellipse at 15% 20%, rgba(200,134,42,0.08) 0%, transparent 50%),
      radial-gradient(ellipse at 85% 80%, rgba(28,43,74,0.06) 0%, transparent 50%)
    `,
        pointerEvents: "none",
        zIndex: 0,
    },
    navbar: {
        position: "relative",
        zIndex: 10,
        display: "flex",
        alignItems: "center",
        gap: "12px",
        padding: "18px 20px",
    },
    navBack: {
        width: "34px",
        height: "34px",
        borderRadius: "10px",
        border: "1.5px solid rgba(28,43,74,0.1)",
        background: "rgba(255,255,255,0.75)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        cursor: "pointer",
        fontSize: "14px",
    },
    navTitle: {
        fontFamily: "'Noto Serif KR', serif",
        fontSize: "16px",
        fontWeight: "700",
        color: "#1C2B4A",
    },
    inner: {
        position: "relative",
        zIndex: 1,
        maxWidth: "640px",
        margin: "0 auto",
        padding: "0 20px 60px",
    },
    header: {
        marginBottom: "24px",
    },
    headerLabel: {
        fontSize: "11px",
        letterSpacing: "0.2em",
        color: "#C8862A",
        textTransform: "uppercase",
        fontWeight: "500",
        marginBottom: "8px",
    },
    headerTitle: {
        fontFamily: "'Noto Serif KR', serif",
        fontSize: "20px",
        fontWeight: "700",
        color: "#1C2B4A",
        lineHeight: "1.4",
    },
    headerDesc: {
        fontSize: "13px",
        color: "#5A6478",
        marginTop: "5px",
        fontWeight: "300",
    },
    myRoomCard: {
        background: "#1C2B4A",
        borderRadius: "22px",
        padding: "22px 24px",
        marginBottom: "24px",
        cursor: "pointer",
        position: "relative",
        overflow: "hidden",
    },
    myRoomTag: {
        fontSize: "10px",
        letterSpacing: "0.15em",
        color: "#E8A84A",
        textTransform: "uppercase",
        marginBottom: "10px",
    },
    myRoomTop: {
        display: "flex",
        alignItems: "center",
        gap: "14px",
        marginBottom: "12px",
    },
    myRoomIcon: {
        width: "48px",
        height: "48px",
        borderRadius: "14px",
        background: "rgba(200,134,42,0.2)",
        border: "1.5px solid rgba(200,134,42,0.4)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: "22px",
        flexShrink: 0,
    },
    myRoomName: {
        fontFamily: "'Noto Serif KR', serif",
        fontSize: "17px",
        fontWeight: "700",
        color: "#fff",
    },
    myRoomSub: {
        fontSize: "12px",
        color: "rgba(255,255,255,0.55)",
        marginTop: "3px",
    },
    myRoomBottom: {
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
    },
    myRoomLast: {
        fontSize: "12px",
        color: "rgba(255,255,255,0.5)",
        flex: 1,
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
        marginRight: "12px",
    },
    unreadBadge: {
        background: "#C8862A",
        color: "#fff",
        fontSize: "10px",
        fontWeight: "700",
        padding: "3px 8px",
        borderRadius: "10px",
        flexShrink: 0,
    },
    sectionLabel: {
        fontSize: "11px",
        letterSpacing: "0.15em",
        color: "#8A95A8",
        textTransform: "uppercase",
        marginBottom: "12px",
    },
    roomList: {
        display: "flex",
        flexDirection: "column",
        gap: "10px",
    },
    roomCard: {
        background: "rgba(255,255,255,0.75)",
        backdropFilter: "blur(12px)",
        border: "1px solid rgba(28,43,74,0.1)",
        borderRadius: "18px",
        padding: "16px 18px",
        display: "flex",
        alignItems: "center",
        gap: "14px",
        cursor: "pointer",
    },
    roomIcon: {
        width: "44px",
        height: "44px",
        borderRadius: "12px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: "20px",
        flexShrink: 0,
    },
    roomInfo: {
        flex: 1,
        minWidth: 0,
    },
    roomName: {
        fontSize: "14px",
        fontWeight: "600",
        color: "#1C2B4A",
        marginBottom: "3px",
    },
    roomPreview: {
        fontSize: "12px",
        color: "#8A95A8",
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    roomRight: {
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-end",
        gap: "5px",
        flexShrink: 0,
    },
    roomTime: {
        fontSize: "10px",
        color: "#8A95A8",
    },
    roomOnline: {
        display: "flex",
        alignItems: "center",
        gap: "4px",
        fontSize: "10px",
        color: "#8A95A8",
    },
    onlineDot: {
        width: "6px",
        height: "6px",
        borderRadius: "50%",
        background: "#4CAF50",
    },
};