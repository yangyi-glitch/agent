package lmy.agent.vo.req;

public class LoginReq {
    private String account;
    private String password;

    public LoginReq() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
