package be.unamur.snail.config;

import java.util.Map;

public class StageConfig {
    private String type;
    private Map<String, Object> params;

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    public Map<String, Object> getParams() {return params;}
    public void setParams(Map<String, Object> params) {this.params = params;}
}
