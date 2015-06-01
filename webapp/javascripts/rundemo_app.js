(function(w) {
	var timer;
	var rundemo_app = {
		"resChange" : function() {
			w.resModified = true;
		},
		"resBlur" : function() {
			if (w.resModified) {
				rundemo_app.saveRes();
			}
		},
		"saveRes" : function() {
			var param = new Object();
			param.pageid = w.pageid;
			param.resFileName = $("#resTab > li[class='active']").children("a")
					.text();
			param.res = w.resEditor.getValue();
			var url = w.contextpath + '/' + w.app + '/saveRes';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.saveResDone,
				error : rundemo_app.httpError
			});
		},
		"saveResDone" : function(data) {
			if (data.success == false) {
				rundemo_app.appError("保存resource资源文件时发生错误", data.errorMsg);// TODO
				// title作为参数，如“保存失败”
			} else {
				window.resModified = false;
			}
		},
		"loadRes" : function(resFileName) {
			var param = new Object();
			param.resFileName = resFileName;
			param.pageid = w.pageid;
			var url = w.contextpath + '/' + w.app + '/loadRes';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : rundemo_app.loadResDone,
				error : rundemo_app.httpError
			});
		},
		"loadResDone" : function(data) {
			if (data.success == false) {
				rundemo_app.appError("加载resource资源文件时发生错误", data.errorMsg);
			} else {
				window.resEditor.setValue(data.res);
				window.resEditor.moveCursorTo(0, 0);
				window.resModified = false;
			}
		},
		"loadCode" : function(javaFilePath) {
			var param = new Object();
			param.javaFilePath = javaFilePath;
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
				rundemo_app.appError("加载代码时发生错误", data.errorMsg);
			} else {
				window.editor.setValue(data.code);
				window.editor.moveCursorTo(0, 0);
				// 初始化code时间戳
				window.codeLastModifiedTime = new Date();
				window.codeLastCompiledTime = 0;
				window.tempCodeLastCompiledTime;
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
				rundemo_app.appError("尝试编译时发生错误", data.errorMsg);
			} else {
				// 编译完成，设置className
				w.className = data.className;
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
			param.className = w.className;
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
				rundemo_app.appError("尝试运行时发生错误", data.errorMsg);
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
				rundemo_app.appError("访问控制台时发生错误", data.errorMsg);
				// 按钮变换
				$('#runButton').show();
				$('#shutdownButton').hide();
			} else {
				// 显示到编译控制台
				$('#console').text($('#console').text() + data.content);// append()和html()一样不做转义，所以使用text()
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
				rundemo_app.appError("关闭时发生错误", data.errorMsg);
			}
		},
		"modifyCode" : function() {
			w.codeLastModifiedTime = new Date();
		},		
		"changeJavaHash" : function(index) {
			var hash = window.location.hash;
			var newHash = "";
			if (hash.length > 0) {// 去掉#号
				hash = hash.substring(1);
				$.each(hash.split('&'), function(i, part) {
					var keyValue = part.split('=');
					if (keyValue[0] == 'j') {
						newHash += "&j=" + index;
					} else {
						newHash += '&' + part;
					}
				});
				newHash = newHash.substring(1);
			} else {
				newHash = "r=0&j=" + index;
			}
			window.location.hash = newHash;
		},
		"changeResHash" : function(index) {
			var hash = window.location.hash;
			var newHash = "";
			if (hash.length > 0) {// 去掉#号
				hash = hash.substring(1);
				$.each(hash.split('&'), function(i, part) {
					var keyValue = part.split('=');
					if (keyValue[0] == 'r') {
						newHash += "&r=" + index;
					} else {
						newHash += '&' + part;
					}
				});
				newHash = newHash.substring(1);
			} else {
				newHash = "j=0&r=" + index;
			}
			window.location.hash = newHash;
		},
		"onHashChange" : function(index) {
			var hash = window.location.hash;
			if (hash.length > 0) {
				// 去掉#号
				hash = hash.substring(1);
				var j, r;
				$.each(hash.split('&'), function(i, part) {
					var keyValue = part.split('=');
					if (keyValue[0] == 'j') {
						j = keyValue[1];
						rundemo_app.changeJavaCodeFile(keyValue[1]);
					} else if (keyValue[0] == 'r') {
						r = keyValue[1];
						rundemo_app.changeResourceFile(keyValue[1]);
					}
				});
				if (j == 0 && r == 0) {
					window.location.hash = "";
				}
			} else {
				if(index!="0"){
					rundemo_app.changeJavaHash(index);
				}
				rundemo_app.changeJavaCodeFile(index);
				rundemo_app.changeResourceFile(0);
			}
		},
		"changeJavaCodeFile" : function(index) {
			//通过输入链接展示代码并选中文件树中的文件
			var curDiv = $(".tree-selected");
			if(curDiv.length == 0){
				rundemo_app.changeSelectedStyle(index);
			}else{
				var curA = curDiv.children("div").children("a");
				filePath = curA.attr("filePath");
				var oldIndex = curA.attr("id");
				if( oldIndex != index){
					curDiv.removeClass("tree-selected");
					curDiv.children("i").attr("class","tree-dot");
					
					rundemo_app.changeSelectedStyle(index);
				}
			}
			
			var filePath = $("#"+index).attr("filePath");
			if(typeof(filePath) == 'undefined'){
				rundemo_app.appError("Error", "You visit the link does not exist, please check and re-visit!");
			}else{
				rundemo_app.loadCode(filePath);
			}
		},
		"changeSelectedStyle" : function(index){
			var temp = $("#"+index).parent().parent();
			temp.addClass("tree-selected");
			temp.children("i").attr("class","icon-ok")
			temp.parent().attr("display","block");
		},
		"changeResourceFile" : function(index) {
			// 当前的index是多少
			var curLi = $("#resTab > li[class='active']");
			var curIndex = curLi.index();
			// 如果更改了，则
			if (index != curIndex) {
				// 更改active
				curLi.removeClass("active");
				var newLi = $("#resTab > li:eq(" + index + ")");
				newLi.addClass("active");
				// 重新加载code
				rundemo_app.loadRes(newLi.children("a").text());
			}
		},
		"input" : function(event) {
			if (event.keyCode == 13) {// enter，发送input内容
				var param = new Object();
				param.pageid = w.pageid;
				param.input = $("#consoleInput").val();
				var url = w.contextpath + '/' + w.app + '/input';
				$.ajax({
					type : 'POST',
					url : url,
					data : param,
					dataType : "json",
					success : rundemo_app.inputDone,
					error : rundemo_app.httpError
				});
			}
		},
		"inputDone" : function(data) {
			if (data.success == false) {
				rundemo_app.appError("输入控制台时发生错误", data.errorMsg);
			} else {
				$("#consoleInput").val("");
			}
		},
		"onunload" : function() {
			var param = new Object();
			param.pageid = w.pageid;
			var url = w.contextpath + '/' + w.app + '/deleteJavaProject';
			$.ajax({
				type : 'POST',
				async: false,
				url : url,
				data : param,
				dataType : "json"
			});
		},
		"appError" : function(title, errorMsg) {
			rundemo_app.alertError(title, errorMsg);
		},
		"httpError" : function(xhr, textStatus, errorThrown) {
			rundemo_app.alertError('网络错误', 'error! (status:' + textStatus
					+ ',msg:' + errorThrown + ')');
		},
		"alertError" : function(title, errorMsg) {
			// 动态显示和隐藏错误消息
			$('#error_Msg > div > h4').text(title+": "+errorMsg);
			$('#error_Msg').show(500);
			setTimeout("$('#error_Msg').hide(1000);",10000);
		},
		"createApp" : function(){
			if (!$('#form').validate().form()) {
				return false;
			}
			$.ajax({
				type : $('#form').attr('method'),
				url : $('#form').attr('action'),
				data : $('#form').serialize(),
				dataType : "json",
				success : function(data) {
					if (data.success == false) {
						$('#progressModal').modal('hide');
						rundemo_app.appError("错误", data.errorMsg);
					} else {
						window.location.href = w.contextpath + '/' + data.app;
					}
				},
				error : function(xhr, textStatus, errorThrown){
					$('#progressModal').modal('hide');
					rundemo_app.httpError(xhr, textStatus, errorThrown);
				}
			});
			$('#createAppModal').modal('hide');
			$('#progressModal').modal('show');
		},
		"updateApp" : function(app){
			var param = new Object();
			param.app = app;
			var url = w.contextpath + '/update';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : function(data) {
					if (data.success == false) {
						$('#progressModal1').modal('hide');
						rundemo_app.appError("错误", data.errorMsg);
					} else {
						window.location.href = w.contextpath + '/' + data.app;
					}
				},
				error : function(xhr, textStatus, errorThrown){
					$('#progressModal1').modal('hide');
					rundemo_app.httpError(xhr, textStatus, errorThrown);
				}
			});
			$('#progressModal1').modal('show');
		}
	};
	w.rundemo_app = rundemo_app;
}(window || this));
