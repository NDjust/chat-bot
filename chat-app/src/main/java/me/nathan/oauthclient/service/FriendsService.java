package me.nathan.oauthclient.service;

import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.model.dto.response.api.FriendsDto;
import me.nathan.oauthclient.model.dto.request.api.NewFriendDto;
import me.nathan.oauthclient.model.dto.response.api.UserDto;
import me.nathan.oauthclient.domain.Friends;
import me.nathan.oauthclient.domain.User;
import me.nathan.oauthclient.repository.FriendsRepository;
import me.nathan.oauthclient.repository.query.UserQueryRepository;
import me.nathan.oauthclient.util.common.ResponseMessage;
import me.nathan.oauthclient.util.common.ResponseStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendsService {

    private FriendsRepository friendsRepository;

    private UserQueryRepository userQueryRepository;

    @Autowired
    public FriendsService(FriendsRepository friendsRepository, UserQueryRepository userQueryRepository) {
        this.friendsRepository = friendsRepository;
        this.userQueryRepository = userQueryRepository;
    }

    public DefaultResponse getFriends(Long userId) {
        List<UserDto> friendDtos = findFriends(userId);
        return DefaultResponse.response(new FriendsDto(friendDtos), ResponseStatusCode.SUCCESS, ResponseMessage.FRIENDS_SEARCH_SUCCESS);
    }

    private List<UserDto> findFriends(Long userId) {
        List<User> friends = userQueryRepository.findFriends(userId);
        return friends.stream()
                .map(friend -> UserDto.builder()
                        .id(friend.getId())
                        .name(friend.getName())
                        .image(friend.getProfileImage())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public DefaultResponse addFriend(Long userId, NewFriendDto newFriendDto) {
        try {
            Long friendId = newFriendDto.getId();
            validFriend(userId, friendId);
            Friends friends = Friends.builder()
                    .userId(userId)
                    .friendId(friendId)
                    .build();
            friendsRepository.save(friends);
            return DefaultResponse.response(ResponseStatusCode.SUCCESS, ResponseMessage.FRIENDS_CREATE_SUCCESS);
        } catch (IllegalArgumentException argumentException) {
            return DefaultResponse.response(ResponseStatusCode.METHOD_NOT_ALLOWED, argumentException.getMessage());
        }

    }

    /** 나 자신 혹은 이미 등록된 friend 등록 요청시 예외 처리.
     *
     * @param userId : 친구 요청 userId
     * @param friendId : 추가할 친구 id
     */
    private void validFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("나 자신을 친구로 등록할 수 없습니다.");
        }

        Friends previous = friendsRepository.findByUserIdAndFriendId(userId, friendId)
                .orElse(null);

        if (previous != null) {
            throw new IllegalArgumentException(
                    String.format("friend Id : %d, user Id : %d - 이미 친구인 관계입니다.", friendId, userId));
        }
    }

    @Transactional
    public DefaultResponse deleteFriend(Long userId, Long friendId) {
        friendsRepository.deleteByUserIdAndFriendId(userId, friendId);
        return DefaultResponse.response(ResponseStatusCode.SUCCESS, ResponseMessage.FRIENDS_DELETE_SUCCESS);
    }
}
