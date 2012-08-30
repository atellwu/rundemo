(function(w) {
	var timer;
	var rundemo_app = {
		"compile" : function() {
			// 发送到服务端，如果保存成功,则继续，否则alert错误信息
			var param = new Object();
			param.pageid = w.pageid;
			param.content = $("#content").val();
			// 发送ajax请求jsonp
			var url = w.contextpath + '/' + w.app + '/compile';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.compileDone,
				error : rundemo_app.sendError
			});
			// disable按钮
			$('#compileButton').attr('disabled', 'disabled');
			// 初始化错误消息
			$('#errorMsg > textarea').val('');
			$('#errorMsg').hide();
		},
		/**
		 * 发送完成
		 */
		"compileDone" : function(data) {
			if (data.success == false) {
				// 显示错误消息
				$('#errorMsg > div[class="modal-body"] > p')
						.text(data.errorMsg);
				// $('#errorMsg').show();
				$('#errorMsg').modal('show');
			} else {
				// 开始显示编译控制台 ????????????????????
				$("#compileConsole").val(data);
				
			}
			// 去掉按钮disable
			$('#compileButton').removeAttr('disabled');
		},
		"sendError" : function(data) {
			// 显示错误消息
			$('#errorMsg > div[class="modal-body"] > p').text(data);
			// $('#errorMsg').show();
			$('#errorMsg').modal('show');
			// 去掉按钮disable
			$('#compileButton').removeAttr('disabled');
		}
	};
	w.rundemo_app = rundemo_app;
}(window || this));
// page loaded
$(document).ready(function() {
	$('#compileButton').removeAttr('disabled');
});
