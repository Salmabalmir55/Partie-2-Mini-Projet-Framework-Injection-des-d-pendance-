package net.balmir.service;


import net.balmir.annotations.Autowired;
import net.balmir.annotations.Component;
import net.balmir.annotations.Qualifier;
import net.balmir.annotations.Scope;
import net.balmir.dao.UserDao;

@Component("userService")
@Scope("singleton")
public class UserServiceImpl implements UserService {

    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;

    @Override
    public String getUser(int id) {
        return userDao.findById(id) + " - Service";
    }
}
