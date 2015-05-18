Array.prototype.in_array = function(e) {
	for (i = 0; i < this.length && this[i] != e; i++)
		;
	return !(i == this.length);
}

$.validator.setDefaults({
	highlight : function(element) {
		$(element).closest('.form-group').addClass('has-error');
	},
	unhighlight : function(element) {
		$(element).closest('.form-group').removeClass('has-error');
	},
	errorElement : 'span',
	errorClass : 'help-block',
	errorPlacement : function(error, element) {
		if (element.parent('.input-group').length) {
			error.insertAfter(element.parent());
		} else {
			error.insertAfter(element);
		}
	}
});

$.validator.addMethod("appName", function(value, element, params) {
	var p = /^[0-9a-zA-Z|_|]*$/;
	if (p.test(value))
		return true;
	return false;
}, "名称中可使用的字符包括：数字、字母、下划线");

$.validator.addMethod("validateURL", function(value, element, params) {
	if (value.indexOf("https://github.com/") == 0 || value.indexOf("git@github.com:") == 0)
		return true;
	return false;
}, "仓库地址格式不正确！");

$('#form').validate({
	rules : {
		app : {
			required : true,
			appName  : true
		},
		gitUrl : {
			required : true,
			validateURL : true
		}
	},
    messages: {
        app: {
            required: "应用名称不能为空！"
        },
        gitUrl : {
			 required : "Git仓库的URL不能为空！"
		}
    }
});
