package business.service.mint;

import cake.web.exchange.ParamInfo;

public class GetWithParameters {
    public String get(@ParamInfo(name = "id") Integer id, @ParamInfo(name = "name") String name, @ParamInfo(name = "salary") Float salary) {
        return "public String get(Integer id, String name, Float salary)";
    }
}
