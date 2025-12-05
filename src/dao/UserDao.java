package dao;
import model.User;
import java.util.List;

public interface UserDao {

    User findByUsername(String username);

    User findById(Long id);

    List<User> findAll();

    void save(User user);

    void update(User user);

    void delete(Long id);
}
