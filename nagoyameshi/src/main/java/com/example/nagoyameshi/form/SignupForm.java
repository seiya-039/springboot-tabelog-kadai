package com.example.nagoyameshi.form;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignupForm {
	@NotEmpty(message = "氏名を入力してください。")
	@Pattern(regexp = ".*[^\\s　]+.*", message = "スペースのみの入力は禁止されています。")
	private String name;

	@NotBlank(message = "フリガナを入力してください。")
	@Pattern(regexp = "^[ァ-ヶー]+([ 　][ァ-ヶー]+)*$", message = "フリガナは全角カタカナで入力してください。")
	private String furigana;

	@NotBlank(message = "郵便番号を入力してください。")
	@Pattern(regexp = "^[0-9]{7}$", message = "郵便番号は7桁の半角数字で入力してください。")
	private String postalCode;

	@NotEmpty(message = "住所を入力してください。")
	@Pattern(regexp = ".*[^\\s　]+.*", message = "スペースのみの入力は禁止されています。")
	private String address;

	@NotBlank(message = "電話番号を入力してください。")
	@Pattern(regexp = "^[0-9]{10,11}$", message = "電話番号は10桁または11桁の半角数字で入力してください。")
	private String phoneNumber;

	@Pattern(regexp = "^$|^[0-9]{8}$", message = "誕生日は8桁の半角数字で入力してください。")
	private String birthday;

	@Pattern(regexp = "^$|.*[^\\s　]+.*", message = "スペースのみの入力は禁止されています。")
	private String occupation;

	@NotBlank(message = "メールアドレスを入力してください。")
	@Email(message = "メールアドレスは正しい形式で入力してください。")
	private String email;

	@NotBlank(message = "パスワードを入力してください。")
	@Length(min = 8, message = "パスワードは8文字以上で入力してください。")
	private String password;

	@NotBlank(message = "パスワード（確認用）を入力してください。")
	private String passwordConfirmation;
}