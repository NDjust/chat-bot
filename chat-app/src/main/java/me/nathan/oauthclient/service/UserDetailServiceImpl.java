package me.nathan.oauthclient.service;

import lombok.extern.slf4j.Slf4j;
import me.nathan.oauthclient.model.dao.MemberInfo;
import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.model.dto.response.api.MembersDto;
import me.nathan.oauthclient.model.dto.response.api.UserDto;
import me.nathan.oauthclient.model.dto.response.api.UsersDto;
import me.nathan.oauthclient.domain.User;
import me.nathan.oauthclient.domain.UserPrincipal;
import me.nathan.oauthclient.repository.query.UserQueryRepository;
import me.nathan.oauthclient.util.exception.ResourceNotFoundException;
import me.nathan.oauthclient.repository.UserRepository;
import me.nathan.oauthclient.util.common.ResponseMessage;
import me.nathan.oauthclient.util.common.ResponseStatusCode;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private UserRepository userRepository;

    private UserQueryRepository userQueryRepository;
    public UserDetailServiceImpl(UserRepository userRepository, UserQueryRepository userQueryRepository) {
        this.userRepository = userRepository;
        this.userQueryRepository = userQueryRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("UserDetailsServiceImpl.loadUserByUsername :::: {}",username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username, please check user info !"));

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );

        return UserPrincipal.create(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );
    }

    public DefaultResponse getUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        List<UserDto> userDtos = users.stream()
                                        .map(user -> UserDto.builder()
                                                .id(user.getId())
                                                .name(user.getName())
                                                .image(user.getProfileImage())
                                                .build())
                                        .collect(Collectors.toList());
        return DefaultResponse.response(new UsersDto(userDtos), ResponseStatusCode.SUCCESS, ResponseMessage.USER_SEARCH_SUCCESS);
    }


    public DefaultResponse<MembersDto> getChatParticipants(Long chatId) {
        List<MemberInfo> users = userQueryRepository.searchMembersByChatId(chatId);
        return DefaultResponse.response(new MembersDto(users), ResponseStatusCode.SUCCESS, ResponseMessage.PARTICIPANTS_SEARCH_SUCCESS);
    }
}
