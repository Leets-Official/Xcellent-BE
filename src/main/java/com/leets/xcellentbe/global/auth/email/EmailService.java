package com.leets.xcellentbe.global.auth.email;

import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.util.Random;

import com.leets.xcellentbe.global.error.exception.custom.InvalidInputValueException;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class EmailService {
	private final RedisService redisService;
	private final JavaMailSender mailSender;
	private int authNumber;

	//임의의 6자리 양수를 반환합니다.
	public void makeRandomNumber() {
		Random r = new Random();
		this.authNumber = 100000 + r.nextInt(900000);
	}


	//mail을 어디서 보내는지, 어디로 보내는지 , 인증 번호를 html 형식으로 어떻게 보내는지 작성합니다.
	public String joinEmail(String email) {
		makeRandomNumber();
		String toMail = email;
		String title = "회원 가입 인증 이메일 입니다."; // 이메일 제목
		String content =
				"인증 번호는 " + authNumber + "입니다." +
				"<br>" +
				"인증번호를 제대로 입력해주세요"; //이메일 내용 삽입
		mailSend(toMail, title, content);
		return Integer.toString(authNumber);
	}

	//이메일을 전송합니다.
	public void mailSend(String toMail, String title, String content) {

		if(redisService.getData(toMail)!=null){
			throw new AuthCodeAlreadySentException();
		}

		MimeMessage message = mailSender.createMimeMessage();//JavaMailSender 객체를 사용하여 MimeMessage 객체를 생성
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");//이메일 메시지와 관련된 설정을 수행합니다.
			// true를 전달하여 multipart 형식의 메시지를 지원하고, "utf-8"을 전달하여 문자 인코딩을 설정
			helper.setTo(toMail);//이메일의 수신자 주소 설정
			helper.setSubject(title);//이메일의 제목을 설정
			helper.setText(content,true);//이메일의 내용 설정 두 번째 매개 변수에 true를 설정하여 html 설정으로한다.
			mailSender.send(message);
		} catch (MessagingException e) {//이메일 서버에 연결할 수 없거나, 잘못된 이메일 주소를 사용하거나, 인증 오류가 발생하는 등 오류
			// 이러한 경우 MessagingException이 발생
			e.printStackTrace();//e.printStackTrace()는 예외를 기본 오류 스트림에 출력하는 메서드
		}
		redisService.setDataExpire(toMail,Integer.toString(authNumber));
	}

	public String checkAuthNum(String email, String authNum) {
		String storedAuthNum = redisService.getData(email);

		if (storedAuthNum == null) {
			return "인증번호가 만료되었습니다.";
		}
		else if(!storedAuthNum.equals(authNum)){
			throw new InvalidInputValueException();
		}
		else {
			return "인증에 성공하였습니다.";
		}
	}
}
