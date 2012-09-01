(function(w) {
	var timer;
	var rundemo_app = {
		"loadCode" : function(javaFileName) {
			var param = new Object();
			param.javaFileName = javaFileName;
			var url = w.contextpath + '/' + w.app + '/loadCode';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.loadCodeDone,
				error : rundemo_app.httpError
			});
		},
		"loadCodeDone" : function(data) {
			if (data.success == false) {
				rundemo_app.appError(data.errorMsg);
			} else {
				window.editor.setValue(data.code);
			}
		},
		"runDemo" : function() {
			// 如果有修改源码，则需要编译
			if (w.codeLastCompiledTime < w.codeLastModifiedTime) {
				rundemo_app.compile();
			} else {
				rundemo_app.run();
			}
		},
		"compile" : function() {
			var param = new Object();
			param.pageid = w.pageid;
			w.tempCodeLastCompiledTime = new Date();
			// param.content = $("#content").val();//不再使用textArea，使用ace开源编辑器
			param.content = w.editor.getValue();
			var url = w.contextpath + '/' + w.app + '/compile';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.compileDone,
				error : rundemo_app.httpError
			});
		},
		"compileDone" : function(data) {
			if (data.success == false) {
				rundemo_app.appError(data.errorMsg);
			} else {
				// 判断编译是否成功
				if (data.content.match("^\\\[info\\\]")) {
					w.codeLastCompiledTime = w.tempCodeLastCompiledTime;// 编译成功才更新CodeLastCompiledTime
					// 开始运行
					rundemo_app.run();
				} else {
					// 显示编译控制台
					$('#console').text(data.content);
				}
			}
		},
		"run" : function() {
			var param = new Object();
			param.pageid = w.pageid;
			var url = w.contextpath + '/' + w.app + '/run';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.runDone,
				error : rundemo_app.httpError
			});
			// 按钮变换
			$('#runButton').hide();
			$('#shutdownButton').show();
		},
		"runDone" : function(data) {
			if (data.success == false) {
				// 显示错误消息
				rundemo_app.appError(data.errorMsg);
				// 按钮变换
				$('#runButton').show();
				$('#shutdownButton').hide();
			} else {
				// 开始显示控制台
				$('#console').text('');
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
				rundemo_app.appError(data.errorMsg);
				// 按钮变换
				$('#runButton').show();
				$('#shutdownButton').hide();
			} else {
				// 显示到编译控制台
				// $('#console').text($('#console').text() + data.content);
				$('#console').append(data.content);
				// $('#console').scrollTop = $('#console').scrollHeight;
				$("#console").scrollTop($("#console")[0].scrollHeight);
				if (data.status == 'continue') {// 继续运行
					rundemo_app.runConsole();
				} else {// 运行已经停止
					$('#runButton').show();
					$('#shutdownButton').hide();
				}
			}
		},
		"shutdown" : function() {
			var param = new Object();
			param.pageid = w.pageid;
			var url = w.contextpath + '/' + w.app + '/shutdown';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.shutdownDone,
				error : rundemo_app.httpError
			});
		},
		"shutdownDone" : function(data) {
			if (data.success == false) {
				rundemo_app.appError(data.errorMsg);
			}
		},
		"modifyCode" : function() {
			w.codeLastModifiedTime = new Date();
		},
		"appError" : function(errorMsg) {
			rundemo_app.alertError(errorMsg);
		},
		"httpError" : function(xhr, textStatus, errorThrown) {
			rundemo_app.alertError('error:' + textStatus + '(' + errorThrown
					+ ')');
		},
		"alertError" : function(errorMsg) {
			// 显示错误消息
			$('#errorMsg > div[class="modal-body"] > p').text(errorMsg);
			$('#errorMsg').modal('show');
		}
	};
	w.rundemo_app = rundemo_app;
}(window || this));
// page loaded
$(document).ready(function() {
	$('#compileButton').removeAttr('disabled');
	$('#runButton').removeAttr('disabled');
});
