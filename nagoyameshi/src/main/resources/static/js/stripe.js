// stripeの公開可能キー
const stripe = Stripe('pk_test_51OYUQTJVi7C9k1KYz28knW5ps689Spyq89TyliZbweYc0nAJSqIzcZFx1XIOxFTPJbMcSRNqD1dCsv9PKnaUKq7Z00CtbSHLos');

const elements = stripe.elements();
const cardElement = elements.create('card');
cardElement.mount('#cardElement');

const cardButton = document.getElementById('cardButton');

cardButton.addEventListener('click', function(e) {
	e.preventDefault();

	// エラーメッセージを初期化する
	document.getElementById('cardHolderNameError').innerHTML = '';
	document.getElementById('cardElementError').innerHTML = '';

	// カード名義人が未入力の場合はエラーメッセージを表示する
	const cardHolderName = document.getElementById('cardHolderName');
	let cardHolderNameError = false;
	if (!cardHolderName.value) {
		document.getElementById('cardHolderNameError').innerHTML = '<div class="text-danger small mb-2">カード名義人の入力は必須です。</div>';
		cardHolderNameError = true;
	}

	stripe.createPaymentMethod({
		type: 'card',
		card: cardElement,
		billing_details: {
			name: cardHolderName.value,
		},
	}).then(function(result) {
		if (result.error) {
			// カード番号に不備がある場合はエラーメッセージを表示する
			document.getElementById('cardElementError').innerHTML = '<div class="text-danger small mb-2">カード番号に不備があります。</div>';
		} else if (!cardHolderNameError) {
			// Payment MethodのIDをサーバーに送信する
			stripePaymentIdHandler(result.paymentMethod.id);
		}
	});
});

function stripePaymentIdHandler(paymentMethodId) {
	const form = document.getElementById('cardForm');

	const hiddenInput = document.createElement('input');
	hiddenInput.setAttribute('type', 'hidden');
	hiddenInput.setAttribute('name', 'paymentMethodId');
	hiddenInput.setAttribute('value', paymentMethodId);
	form.appendChild(hiddenInput);

	form.submit();
}