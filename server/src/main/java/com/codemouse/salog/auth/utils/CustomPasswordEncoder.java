package com.codemouse.salog.auth.utils;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
    소셜 가입한 회원의 경우 db 저장되는 password 가 null 임
    그래서 소셜 가입한 회원이 기본 회원 로그인 시 password 형식이 맞지 않아
    DelegatingPasswordEncoder 에서 500에러가 리턴됨
    그런데 이 에러가 항상 최상위에서 발동되기 때문에 여러 방법을 시도 해봤지만 중간에 가로채서 비지니스 예외를 던질 수가 없음

    그래서 해결법으로
     BCryptPasswordEncoder 를 사용하도록 변경하고 기존 DB를 마이그레이션하거나
     DelegatingPasswordEncoder 를 계속 사용하되 커스텀을 통해 password 가 빈 문자열 혹은 null 인 경우를 핸들링 해야함

     그 중 두 번째 방법을 사용한 결과 이 클래스가 생성됨
     null 값인 password에 대해 false를 리턴하도록 하여 인증 흐름에서 비밀번호가 틀린 경우로 처리되도록 함

     BadCredentialsException 이 발생하도록 하고 해당 에러 리턴을 AuthenticationFailureHandler가 처리하게 함

     이 결과 PASSWORD_MISMATCHED 가 리턴되며, 해당 에러 코드의 내용을 수정함
*/

public class CustomPasswordEncoder implements PasswordEncoder {
    private final PasswordEncoder delegate = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // null 또는 빈 문자열 처리
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return delegate.matches(rawPassword, encodedPassword);
    }
}
