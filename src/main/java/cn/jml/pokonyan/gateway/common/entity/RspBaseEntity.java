package cn.jml.pokonyan.gateway.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
public class RspBaseEntity implements Serializable {
    private static final long serialVersionUID = 549597423164032622L;
    private String            returnCode;
    private String            returnDesc;
}
