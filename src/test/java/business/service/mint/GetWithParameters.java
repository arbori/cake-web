package business.service.mint;

public class GetWithParameters {
    public static record GetParam(Integer id, String name, Float salary) {
    }

    public String get(GetParam param) {
        return String.format("public String get(%s)", param);
    }
}
