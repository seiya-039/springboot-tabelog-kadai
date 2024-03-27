// カテゴリの編集用フォーム
const editCategoryForm = document.forms.editCategoryForm;

// カテゴリの削除用フォーム
const deleteCategoryForm = document.forms.deleteCategoryForm;

// 削除の確認メッセージ
const deleteMessage = document.getElementById('deleteCategoryModalLabel');

// カテゴリの編集用モーダルを開くときの処理
document.getElementById('editCategoryModal').addEventListener('show.bs.modal', (event) => {
  let editButton = event.relatedTarget;
  let categoryId = editButton.dataset.categoryId;
  let categoryName = editButton.dataset.categoryName;

  editCategoryForm.action = `/admin/categories/${categoryId}/update`;
  editCategoryForm.id.value = categoryId;
  editCategoryForm.name.value = categoryName;
});

// カテゴリの削除用モーダルを開くときの処理
document.getElementById('deleteCategoryModal').addEventListener('show.bs.modal', (event) => {
  let deleteButton = event.relatedTarget;
  let categoryId = deleteButton.dataset.categoryId;
  let categoryName = deleteButton.dataset.categoryName;

  deleteCategoryForm.action = `/admin/categories/${categoryId}/delete`;
  deleteMessage.textContent = `「${categoryName}」を削除してもよろしいですか？`
});
