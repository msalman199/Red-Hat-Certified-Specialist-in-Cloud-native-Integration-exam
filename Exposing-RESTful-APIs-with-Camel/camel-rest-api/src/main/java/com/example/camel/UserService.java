package com.example.camel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    private static final Map<Long, User> users = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    static {
        // Initialize with some sample data
        users.put(1L, new User(1L, "John Doe", "john.doe@example.com", 30));
        users.put(2L, new User(2L, "Jane Smith", "jane.smith@example.com", 25));
        users.put(3L, new User(3L, "Bob Johnson", "bob.johnson@example.com", 35));
        idGenerator.set(4L);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUserById(Long id) {
        return users.get(id);
    }

    public User createUser(User user) {
        Long newId = idGenerator.getAndIncrement();
        user.setId(newId);
        users.put(newId, user);
        return user;
    }

    public User updateUser(Long id, User user) {
        if (users.containsKey(id)) {
            user.setId(id);
            users.put(id, user);
            return user;
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        return users.remove(id) != null;
    }

    public int getUserCount() {
        return users.size();
    }
}
