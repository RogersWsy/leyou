package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;
import org.joda.time.DateTime;

@Data
public class ExceptionVo {
    private int status;
    private String msg;
    private String timestamp;

    public ExceptionVo(ExceptionEnum em) {
        this.status = em.getStatus();
        this.msg = em.getMessage();
        this.timestamp = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
    }
}
