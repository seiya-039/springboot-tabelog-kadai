package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.ResetEventPublisher;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.ResetForm;
import com.example.nagoyameshi.form.ResetPasswordForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {
	private final UserService userService;
	private final SignupEventPublisher signupEventPublisher;
	private final VerificationTokenService verificationTokenService;
	private final UserRepository userRepository;
	private final ResetEventPublisher resetEventPublisher;

	public AuthController(UserService userService, SignupEventPublisher signupEventPublisher,
			VerificationTokenService verificationTokenService, UserRepository userRepository,
			ResetEventPublisher resetEventPublisher) {
		this.userService = userService;
		this.signupEventPublisher = signupEventPublisher;
		this.verificationTokenService = verificationTokenService;
		this.userRepository = userRepository;
		this.resetEventPublisher = resetEventPublisher;
	}

	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}

	@GetMapping("/reset")
	public String reset(Model model) {
		model.addAttribute("resetForm", new ResetForm());
		return "auth/reset";
	}

	@PostMapping("/signup")
	public String signup(@ModelAttribute @Validated SignupForm signupForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
		// メールアドレスが登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
		if (userService.isEmailRegistered(signupForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}

		// パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
		if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
			bindingResult.addError(fieldError);
		}

		if (bindingResult.hasErrors()) {
			return "auth/signup";
		}

		User createdUser = userService.create(signupForm);
		String requestUrl = new String(httpServletRequest.getRequestURL());
		signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
		redirectAttributes.addFlashAttribute("successMessage",
				"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。");

		return "redirect:/";
	}

	@GetMapping("/signup/verify")
	public String verify(@RequestParam(name = "token") String token, Model model) {
		VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);

		if (verificationToken != null) {
			User user = verificationToken.getUser();
			userService.enableUser(user);
			String successMessage = "会員登録が完了しました。";
			model.addAttribute("successMessage", successMessage);
		} else {
			String errorMessage = "トークンが無効です。";
			model.addAttribute("errorMessage", errorMessage);
		}

		return "auth/verify";
	}

	@PostMapping("/reset")
	public String reset(@ModelAttribute @Validated ResetForm resetForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
		// 登録済でないメールアドレスであれば、BindingResultオブジェクトにエラー内容を追加する
		if (!userService.isEmailRegistered(resetForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "未登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}

		if (bindingResult.hasErrors()) {
			return "auth/reset";
		}

		User user = userRepository.findByEmail(resetForm.getEmail());
		String requestUrl = new String(httpServletRequest.getRequestURL());
		resetEventPublisher.publishResetEvent(user, requestUrl);
		redirectAttributes.addFlashAttribute("successMessage",
				"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、メールアドレスを再発行してください。");

		return "redirect:/";
	}

	@GetMapping("/reset/verify")
	public String resetVerify(@RequestParam(name = "token") String token, Model model) {
		VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);

		if (verificationToken != null) {
			User user = verificationToken.getUser();
			ResetPasswordForm resetPasswordForm = new ResetPasswordForm();
			resetPasswordForm.setId(user.getId());
			model.addAttribute("resetPasswordForm", resetPasswordForm);
		} else {
			String errorMessage = "トークンが無効です。";
			model.addAttribute("errorMessage", errorMessage);
		}

		return "auth/reset-verify";
	}

	@PostMapping("/reset/verify/update")
	public String resetVerifyUpdate(@ModelAttribute @Validated ResetPasswordForm resetPasswordForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes, Model model) {
		// パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
		if (!userService.isSamePassword(resetPasswordForm.getPassword(), resetPasswordForm.getPasswordConfirmation())) {
			String errorMessage = "パスワードが一致していません。";
			model.addAttribute("errorMessage", errorMessage);
			redirectAttributes.addFlashAttribute("errorMessage",
					"パスワードが一致しませんでした。ブラウザバックして再度入力をお願いします。");
			return "redirect:/";
		}

		userService.updatePassword(resetPasswordForm);
		redirectAttributes.addFlashAttribute("successMessage",
				"パスワードの変更が完了しました。");
		return "redirect:/";
	}
}