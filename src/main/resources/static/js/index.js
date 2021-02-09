$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//发送AJAX请求之前,将CSRF令牌设置到请求的消息头中
	//使用jQuery选择器获取meta元素 指定元素 取出他的内容
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// //在发送请求之前,我们对整个请求做一个设置
	// $(document).ajaxSend(function (e,xhr,options) {
	// 	xhr.setRequestHeader(header,token);
	// });


	//获取标题和内容
	var title = $("#recipient-name").val();
	var content =$("#message-text").val();
	//发送异步请求(POST)
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title,"content":content},
		//回调函数,来处理返回的结果
		function (data) {
			data = $.parseJSON(data);
		//在提示框当中,显示提示的消息
		//	利用id来获取提示框,利用text()动态的获取消息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//2秒后,自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if (data.code ==0){
					window.location.reload();
				}
			}, 2000);
		}
	);


	$("#hintModal").modal("show");
	setTimeout(function(){
		$("#hintModal").modal("hide");
	}, 2000);
}