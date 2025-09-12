// src/main/java/atlan/evently/atlan/user/web/mapper/UserMapper.java
package atlan.evently.atlan.user.web;

import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.user.web.dto.UserResponse;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole().name());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
