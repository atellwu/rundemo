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
				error : rundemo_app.httpError
			});
			// disable按钮
			$('#compileButton').attr('disabled', 'disabled');
		},
		"compileDone" : function(data) {
			if (data.success == false) {
				// 显示错误消息
				$('#errorMsg > div[class="modal-body"] > p')
						.text(data.errorMsg);
				$('#errorMsg').modal('show');
			} else {
				// 开始显示编译控制台
				$('#console').val('');
				rundemo_app.compileConsole();
			}
			// 去掉按钮disable
			$('#compileButton').removeAttr('disabled');
		},
		"compileConsole" : function(data) {
			// 发送到服务端，如果保存成功,则继续，否则alert错误信息
			var param = new Object();
			param.pageid = w.pageid;
			// 发送ajax请求jsonp
			var url = w.contextpath + '/' + w.app + '/compileConsole';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.compileConsoleDone,
				error : rundemo_app.httpError
			});
		},
		"compileConsoleDone" : function(data) {
			if (data.success == false) {
				// 显示错误消息
				$('#errorMsg > div[class="modal-body"] > p')
						.text(data.errorMsg);
				$('#errorMsg').modal('show');
			} else {
				// 显示到编译控制台
				$('#console').val($('#console').val()+data.content); 
				if(data.status == 'continue'){
//					rundemo_app.compileConsole();//compile的不连续访问
				}
			}
		},
		"run" : function() {
			// 发送到服务端，如果保存成功,则继续，否则alert错误信息
			var param = new Object();
			param.pageid = w.pageid;
			// 发送ajax请求jsonp
			var url = w.contextpath + '/' + w.app + '/run';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.runDone,
				error : rundemo_app.httpError
			});
			// disable按钮
			$('#runButton').attr('disabled', 'disabled');
		},
		"runDone" : function(data) {
			console.log('runDone');
			if (data.success == false) {
				// 显示错误消息
				$('#errorMsg > div[class="modal-body"] > p')
						.text(data.errorMsg);
				$('#errorMsg').modal('show');
			} else {
				// 开始显示编译控制台
				$('#console').val('');
				rundemo_app.runConsole();
			}
			// 去掉按钮disable
			$('#runButton').removeAttr('disabled');
		},
		"runConsole" : function(data) {
			// 发送到服务端，如果保存成功,则继续，否则alert错误信息
			var param = new Object();
			param.pageid = w.pageid;
			// 发送ajax请求jsonp
			var url = w.contextpath + '/' + w.app + '/runConsole';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.runConsoleDone,
				error : rundemo_app.httpError
			});
		},
		"runConsoleDone" : function(data) {
			if (data.success == false) {
				// 显示错误消息
				$('#errorMsg > div[class="modal-body"] > p')
						.text(data.errorMsg);
				$('#errorMsg').modal('show');
			} else {
				// 显示到编译控制台
				$('#console').val($('#console').val()+data.content); 
				if(data.status == 'continue'){
					rundemo_app.runConsole();
				}
			}
		},
		"httpError" : function(xhr,textStatus, errorThrown) {
			// 显示错误消息
			$('#errorMsg > div[class="modal-body"] > p').text('error:'+errorThrown.code+'('+errorThrown.msg+')');
			// $('#errorMsg').show();
			$('#errorMsg').modal('show');
			// 去掉按钮disable
			$('#compileButton').removeAttr('disabled');
			$('#runButton').removeAttr('disabled');
		}
	};
	w.rundemo_app = rundemo_app;
}(window || this));
// page loaded
$(document).ready(function() {
	$('#compileButton').removeAttr('disabled');
	$('#runButton').removeAttr('disabled');
});
