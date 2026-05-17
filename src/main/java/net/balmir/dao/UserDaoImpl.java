package net.balmir.dao;

import net.balmir.annotations.Component;

@Component("userDao")
public class UserDaoImpl implements UserDao {
    @Override
    public String findById(int id) {
        return "Utilisateur " + id;
    }
}
