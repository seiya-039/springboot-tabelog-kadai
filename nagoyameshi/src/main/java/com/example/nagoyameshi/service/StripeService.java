package com.example.nagoyameshi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionListParams;

@Service
public class StripeService {
	@Value("${stripe.api-key}")
	private String stripeApiKey;

	public Customer createCustomer(String name, String email, String paymentMethodId) {
		Stripe.apiKey = stripeApiKey;
		Customer customer = null;

		Map<String, Object> customerParams = new HashMap<>();
		customerParams.put("name", name);
		customerParams.put("email", email);

		try {
			customer = Customer.create(customerParams);

			Map<String, Object> params = new HashMap<>();
			params.put("customer", customer.getId());
			PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
			paymentMethod.attach(params);

			Map<String, Object> updateParams = new HashMap<>();
			updateParams.put("invoice_settings", Map.of("default_payment_method", paymentMethod.getId()));
			customer.update(updateParams);
		} catch (StripeException e) {
			e.printStackTrace();
			throw new RuntimeException("Stripeに顧客を作成した際にエラーが発生しました。", e);
		}

		return customer;
	}

	public Subscription createSubscription(String customerId, String planId) {
		Stripe.apiKey = stripeApiKey;

		Map<String, Object> item = new HashMap<>();
		item.put("plan", planId);

		Map<String, Object> items = new HashMap<>();
		items.put("0", item);

		Map<String, Object> params = new HashMap<>();

		params.put("customer", customerId);
		params.put("items", items);

		try {
			return Subscription.create(params);
		} catch (StripeException e) {
			throw new RuntimeException(e);
		}
	}

	public PaymentMethod getDefaultPaymentMethod(String customerId) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("customer", customerId);
			params.put("type", "card");
			PaymentMethodCollection paymentMethods = PaymentMethod.list(params);

			// 顧客が支払い方法を追加しているか確認する
			if (!paymentMethods.getData().isEmpty()) {
				return paymentMethods.getData().get(0);
			}
		} catch (StripeException e) {
			e.printStackTrace();
		}

		// 顧客が支払い方法を追加していない場合、nullを返す
		return null;
	}

	public void attachPaymentMethodToCustomer(String customerId, String paymentMethodId) throws StripeException {
		PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
		Map<String, Object> params = new HashMap<>();
		params.put("customer", customerId);
		paymentMethod.attach(params);
	}

	public void updateSubscription(String customerId, String paymentMethodId) throws StripeException {
		attachPaymentMethodToCustomer(customerId, paymentMethodId);
		Map<String, Object> params = new HashMap<>();
		params.put("invoice_settings", Map.of("default_payment_method", paymentMethodId));
		Customer customer = Customer.retrieve(customerId);
		customer.update(params);

	}

	public String getDefaultPaymentMethodId(String customerId) throws StripeException {
		Customer customer = Customer.retrieve(customerId);
		return customer.getInvoiceSettings().getDefaultPaymentMethod();
	}

	public void detachPaymentMethod(String paymentMethodId) throws StripeException {
		PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
		paymentMethod.detach();

	}

	public Subscription getSubscription(String customerId) {
		try {
			SubscriptionListParams params = SubscriptionListParams.builder()
					.setCustomer(customerId)
					.build();

			List<Subscription> subscriptions = Subscription.list(params).getData();

			for (Subscription subscription : subscriptions) {
				if ("active".equals(subscription.getStatus())) {
					return subscription;
				}
			}

			return null;
		} catch (StripeException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void cancelSubscription(Subscription subscription) {
		try {
			subscription.cancel();
		} catch (StripeException e) {
			e.printStackTrace();
		}
	}
}
