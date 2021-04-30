package me.nathan.oauthclient.util.common;

public class ResponseMessage {

    // FRIENDS RESPONSE MESSAGE
    public static final String FRIENDS_CREATE_SUCCESS        = "친구 등록 성공";
    public static final String FRIENDS_CREATE_FAIL           = "친구 등록 실패";
    public static final String FRIENDS_DELETE_SUCCESS        = "친구 삭제 성공";
    public static final String FRIENDS_DELETE_FAIL           = "친구 삭제 실패";
    public static final String FRIENDS_SEARCH_SUCCESS        = "친구 조회 성공";
    public static final String FRIENDS_SEARCH_FAIL           = "친구 조회 실패";

    // USER RESPONSE MESSAGE
    public static final String USER_SEARCH_SUCCESS           = "유저 조회 성공";
    public static final String USER_SEARCH_FAIL              = "유저 조회 실패";

    // PARTICIPANTS RESPONSE MESSAGE
    public static final String PARTICIPANTS_SEARCH_SUCCESS   = "채팅 멤버 조회 성공";
    public static final String PARTICIPANTS_SEARCH_FAIL      = "채팅 멤버 조회 실패";

    // MESSAGE
    public static final String MESSAGE_CREATE_SUCCESS          = "메세지 등록 성공";
    public static final String MESSAGE_CREATE_FAIL             = "메세지 등록 실패";
    public static final String MESSAGE_SEARCH_SUCCESS          = "메세지 조회 성공";
    public static final String MESSAGE_SEARCH_FAIL             = "메세지 조회 실패";

    // CHAT RESPONSE MESSAGE
    public static final String CHAT_CREATE_SUCCESS        = "채팅 등록 성공";
    public static final String CHAT_CREATE_FAIL           = "채팅 등록 실패";
    public static final String CHAT_OUT_SUCCESS           = "채팅 나가기 성공";
    public static final String CHAT_OUT_FAIL              = "채팅 나가기 실패";
    public static final String CHAT_SEARCH_SUCCESS        = "채팅 조회 성공";
    public static final String CHAT_SEARCH_FAIL           = "채팅 조회 실패";
    public static final String CHAT_READ_SUCCESS          = "채팅 읽음 성공";
    public static final String CHAT_READ_FAIL             = "채팅 읽음 실패";
    public static final String CHAT_NOT_FOUND             = "존재하지 않은 채팅";

    // OTHER MESSAGE
    public static final String DB_ERROR                    = "디비 에러";
    public static final String INTERNAL_SERVER_ERROR       = "서버 내부 에러";
    public static final String BAD_REQUEST                 = "잘못된 요청입니다.";
    public static final String UNAUTHORIZED                = "인증 실패.";
    public static final String AUTHORIZED                  = "인증 성공.";
}
