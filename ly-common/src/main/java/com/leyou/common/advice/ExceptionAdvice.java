package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionVo> handleException(LyException e){
        ExceptionVo vo = new ExceptionVo(e.getEm());
        return ResponseEntity.status(vo.getStatus()).body(vo);
    }
}
