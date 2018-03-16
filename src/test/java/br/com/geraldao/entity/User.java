package br.com.geraldao.entity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TUnpbxUser")
@AttributeOverride(name = "id", column = @Column(name = "id", insertable = false, updatable = false))
public class User extends BaseEntity {
    /**
     * 
     */
    private static final long serialVersionUID = 1536569963155884826L;
    private String            login;
    private String            passwordHint;
    private String            oldPassword;

    @Column(name = "Login")
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHint() {
        return passwordHint;
    }

    public void setPasswordHint(String passwordHint) {
        this.passwordHint = passwordHint;
    }

    public String getOldPassword() {
        return oldPassword;
    }
}
