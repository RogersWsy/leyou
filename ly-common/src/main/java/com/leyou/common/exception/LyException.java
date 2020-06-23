package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

@Getter
public class LyException extends RuntimeException{
    private int status;
    private ExceptionEnum em;

    public LyException(ExceptionEnum em) {
        super(em.getMessage());
        this.status = em.getStatus();
    }

    public LyException(ExceptionEnum em, Throwable cause) {
        super(em.getMessage(), cause);
        this.status = em.getStatus();
    }

}
