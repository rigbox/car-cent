var RIG = {
		
	rs:{},
	_data_table: {},

	rowSelected:function() {
		var dataTable=$('#data-table').DataTable();
		$('#data-table tbody').on( 'click', 'tr', function () {
		     if ( $(this).hasClass('selected') ) {
		         $(this).removeClass('selected');
		     } else {
		    	 dataTable.$('tr.selected').removeClass('selected');
		         $(this).addClass('selected');
		     }
		});
	},
	
	rowsSelected:function() {
		$('#data-table tbody').on( 'click', 'tr', function () {
		    $(this).toggleClass('selected');
		});
	},
	

	initRemoveButton:function(idName) {
		$('#removeButton').on('click', function () {
			var dataTable=$('#data-table').DataTable();
			var rs = dataTable.row('.selected').data();
			if(rs==undefined||rs.s==''){
				IOT.msgModal('请先选中一行记录!');
				return;
			}
		    $("#removeModal").modal({
		    	backdrop:false,
		    });
	    });
		
		$("#removeSubmit").on('click', function () {
	    	var s = ''+idName;
	    	var dataTable=$('#data-table').DataTable();
		    var rs = dataTable.row('.selected').data();
		    if (!$('#removeForm').valid()) {
				return true;
			}
			$(".modal-dialog").children(".box-primary").append("<div id='overlaydiv' class='overlay'><i class='fa fa-refresh fa-spin'></i></div>");
		    $.ajax({
		        type: "post",
		        url: $('#removeButton').attr("ref")+"?"+s+"="+rs[s],
		        async: true,
		        success: function(result){
					$(".overlay").remove();
		            var result = eval('('+result+')');
		            if(response.code=='9000'){//上传成功
						RIG.msgSuccess(response.message);
						//重新加载表格数据
						var _url = $('#refreshButton').attr("ref")+'?'+$('#searchForm').serialize();
						RIG._data_table.ajax.url(_url).load();
					}else{
						//保存失败
						RIG.msgFailing(response.message);
					}
		        }
		    });
	  	});
	},
	
	
	initSearchButton:function() {
		$('#refreshButton').click( function () {
			//IOT.menu_click($('#refreshButton').attr("ref")+'?forward=forward');
			var _url = $('#refreshButton').attr("ref")+'?'+$('#searchForm').serialize();
			RIG._data_table.ajax.url(_url).load();
		});
	},
	
	initCreateSubmit:function() {
		$('#createSubmit').on('click', function() {
			if (!$('#createForm').valid()) {
				return false;
			}
			//不可操作覆盖层
			$(".modal-dialog").children(".card").append("<div id='overlaydiv' class='overlay'><i class='fa fa-refresh fa-spin'></i></div>");
			var _url = $('#createForm').attr("action");
			var _data = RIG.GetForm($('#createForm'));
			var _callback = $('#createForm').attr("callback")+"?"+ $('#searchForm').serialize();
			RIG.ajaxJson(_url,_data,function(response) {
				$(".overlay").remove();
				if(response.code=='9000'){//上传成功
					RIG.msgSuccess(response.message);
					$('#createModal').modal('toggle'); // close the dialog
					$('#createModalLabel').popover('destroy');
					//重新加载表格数据
					var _url = $('#refreshButton').attr("ref")+'?'+$('#searchForm').serialize();
					RIG._data_table.ajax.url(_url).load();
				}else{
					//保存失败
					$('#createModalLabel').popover({
						content : response.message
					});
					$('#createModalLabel').popover('show');
					//保存失败
					RIG.msgFailing(response.message);
				}
			});
			
		});
	},
	

	initModifySubmit:function() {
		$('#modifySubmit').on('click', function () {
		    if (!$('#modifyForm').valid()) {
			    return false;
			}
			$(".modal-dialog").children(".card").append("<div id='overlaydiv' class='overlay'><i class='fa fa-refresh fa-spin'></i></div>");
			var _url = $('#modifyForm').attr("action");
			var _data = RIG.GetForm($('#modifyForm'));
			var _callback = $('#modifyForm').attr("callback")+"?"+ $('#searchForm').serialize();
			RIG.ajaxJson(_url,_data,function(response) {
				$(".overlay").remove();
				if(response.code=='9000'){//上传成功
					RIG.msgSuccess(response.message);
					$('#modifyModal').modal('hide');        // close the dialog
	                $('#modifyModalLabel').popover('destroy');
					//重新加载表格数据
					var _url = $('#refreshButton').attr("ref")+'?'+$('#searchForm').serialize();
					RIG._data_table.ajax.url(_url).load();
				}else{
					//保存失败
					$('#modifyModalLabel').popover({
						content : response.message
					});
					$('#modifyModalLabel').popover('show');
					//保存失败
					RIG.msgFailing(response.message);
				}
			});
		});
	},

	loadSelectOption:function(_select,_data,callback) {
		if(_select) {
			var _url = _select.attr('data-url');
			RIG.ajaxRequest(_url,_data,function(response) {
				var selNodes=response;
				var html="";
				for(var i=0;i<selNodes.length;i++){
					html+="<option value='"+selNodes[i]['key']+"'>"+selNodes[i]['value']+"</option>";
				}
				_select.html(html);
				if(callback) {
            		callback();  
                    return;
                }
			});
		};
	},
	
	loadCheckBox:function(_checkbox,_data,callback) {
		if(_checkbox) {
			var _url = _checkbox.attr('data-url');
			RIG.ajaxRequest(_url,_data,function(response) {
				var selNodes=response;
				var html="";
				for(var i=0;i<selNodes.length;i++){
					html+="<input name='industry' class='input_check' type='checkbox' value='"
							+selNodes[i]['key']+"'/>"+selNodes[i]['value'];
				}
				_checkbox.html(html);
				if(callback) {
            		callback();  
                    return;
                }
			});
		};
	},
	
	
	
	msgSuccess : function(message) {
		var datetime = new Date();
		var hour = datetime.getHours();
        var minute = datetime.getMinutes();
        var second = datetime.getSeconds();
        if(message.length>10) {
        	//message = message.substr(0,10);
        }
        var li = $('<div class="dropdown-divider"></div><a href="javascript:void(0)" onclick="RIG.initMsgModal(this);" class="dropdown-item"><div style="overflow:hidden"><i class="fa fa-hand-peace-o text-yellow"></i>  '
		+message+'</div>  <span class="float-right text-muted text-sm">'+hour+":"+minute+":"+second+"</span></a>");
//		var li = $("<li><a href='javascript:void(0)' onclick='IOT.initMsgModal(this);'><i class='fa fa-hand-peace-o text-yellow'></i>" +
//				"  <small><i class='fa fa-clock-o'></i>"
//				+hour+":"+minute+":"+second+"</small> "+message+"</a></li>");
		$("#sys-message-list").prepend(li);
		var count = 1 + parseInt($("#sys-message-count").text());
		$(".sys-message-count").text(count);
		$("#sys-message-count").text(count);
		$('#sys-message').dropdown('toggle');
	},
	
	msgFailing : function(message) {
		var datetime = new Date();
		var hour = datetime.getHours();
        var minute = datetime.getMinutes();
        var second = datetime.getSeconds();
        if(message.length>10) {
        	//message = message.substr(0,10);
        }
//		var li = $("<li><a href='javascript:void(0)' onclick='RIG.initMsgModal(this);'><i class='fa fa-warning text-red'></i>" +
//				"  <small><i class='fa fa-clock-o'></i>"
//				+hour+":"+minute+":"+second+"</small> "+message+"</a></li>");
		var li = $('<div class="dropdown-divider"></div><a href="javascript:void(0)" onclick="RIG.initMsgModal(this);" class="dropdown-item"><div style="overflow:hidden"><i class="fa fa-warning text-red"></i>  '
					+message+'</div>  <span class="float-right text-muted text-sm">'+hour+":"+minute+":"+second+"</span></a>");
		$("#sys-message-list").prepend(li);
		var count = 1 + parseInt($("#sys-message-count").text());
		$("#sys-message-count").text(count);
		$(".sys-message-count").text(count);
		$('#sys-message').dropdown('toggle');
	},
	
	initMsgModal : function(node) {
		$("#msgModalContent").html($(node).html());
        $("#msgModal").modal('toggle');
	},
		
    msgModal : function(content) {
        $("#msgModalContent").html(content);
        $("#msgModal").modal('toggle');
    },

    msgModalEvent :  function(content) {
        $("#msgModalContent").html(content);
        $("#msgModal").modal({
        	backdrop:true,
        });
        $('#msgModal').on('hidden.bs.modal', function (e) {
        	window.top.location.href="login";
      	});
    },
	
	openConfirmModal : function(content) {
		$("#confirmContent").html("");
	    //$("#confirmContent").html(content);
	    $("#confirmModal").modal({
	    	backdrop:false,
	    });
	},
	
	initConfirmSubmit:function() {
		$("#confirmModalSubmit").on('click',function(){
	    	$(".modal-dialog").children(".card").append("<div id='overlaydiv' class='overlay'><i class='fa fa-refresh fa-spin'></i></div>");
	    	var _url = $("#confirmForm").attr("action");
	    	var _data = RIG.GetForm($('#confirmForm'));
	    	RIG.ajaxJson(_url,_data,function(response){
	    		$(".overlay").remove();
	    		if(response.code=='9000'){
	    			RIG.msgSuccess(response.message);
	    			$('#confirmModal').modal('toggle'); // close the dialog
	    			$('#confirmModalLabel').popover('destroy');
	    			//重新加载表格数据
	    			var _url = $('#refreshButton').attr("ref")+'?'+$('#searchForm').serialize();
	    			RIG._data_table.ajax.url(_url).load();
	    		}else{
	    			//保存失败
	    			$('#confirmModalLabel').popover({
	    				content : response.message
	    			});
	    			$('#confirmModalLabel').popover('show');
	    			//保存失败
	    			RIG.msgFailing(response.message);
	    		}
	    	});
	    });
	},

	/*确认提示框*/
	confrim:function(content){
		$("#confirmModalDody").html(content);  //内容初始化
		$("#confirmModalContent").modal('toggle');; //确认框显示
		return {
			on:function(callback){
				if(callback&&callback instanceof Function){
					$("#confirmModalCancel").unbind("click");//取消.选择
					$("#confirmCodalConfrim").unbind("click");//确定.选择
					$("#confirmModalCancel").click(function(){//取消点击后
						$("#confirmModalContent").hide();//确认框隐藏
						callback(false);
					});
					$("#confirmCodalConfrim").click(function(){
						$("#confirmModalContent").hide();
						callback(true);
					});
				}
			}
		}
	},
	

	ajaxRequest :function(url,data,callback){
		var _this=this;
		$.ajax({
			// 后台处理程序
			url : url,
			// 数据发送方式
			type : "post",
			// 接受数据格式
			dataType : "json",
			// 要传递的数据
			data : data,
			async: true,
			// 回传函数
            timeout:300000,
            success:function(response, textStatus, request){ 
            	//alert(JSON.stringify(response));
            	//判断是否登录超时或权限错误
            	if(callback) {
            		callback(response);  
                    return;
                }
			}
		});
	},
	ajaxJson :function(url,data,callback){
		var _this=this;
		$.ajax({
			// 后台处理程序
			url : url,
			// 数据发送方式
			type : "post",
			// 接受数据格式
			dataType : "json",
			// 要传递的数据
			data : data,
			async: true,
			// 回传函数
            timeout:300000,
            success:function(response, textStatus, request){ 
            	//alert(JSON.stringify(response));
            	//判断是否登录超时或权限错误
            	if(callback) {
            		callback(response);  
                    return;
                }
			}
		});
	},
	ajaxHtml :function(url,data,callback){
		var _this=this;
		$.ajax({
			// 后台处理程序
			url : url,
			// 数据发送方式
			type : "post",
			// 要传递的数据
			data : data,
			async: true,
			// 回传函数
            timeout:30000,// 设置请求超时时间（毫秒）。   
            success:function(response){ 
            	//判断是否登录超时或权限错误
            	if(callback) {
            		callback(response);  
                    return;
                }
			}
		});
	},
	ajaxGetHtml :function(url,data,callback){
		var _this=this;
		$.ajax({
			// 后台处理程序
			url : url,
			// 数据发送方式
			type : "get",
			// 要传递的数据
			data : data,
			async: true,
			// 回传函数
            timeout:30000,// 设置请求超时时间（毫秒）。   
            success:function(response){ 
            	//判断是否登录超时或权限错误
            	if(callback) {
            		callback(response);  
                    return;
                }
			}
		});
	},

	/*获取表单信息*/
	GetForm:function(node){
		var parameter={};
		node.find("input[type=text]").each(function(k,e){
//			if($(e).val()){
//				parameter[$(e).attr('name')]=$(e).val();
//			}
			if($(e).val()){
				if(!parameter[$(e).attr('name')]){
					parameter[$(e).attr('name')]=$(e).val();
				}else{
					parameter[$(e).attr('name')]+=","+$(e).val();
				}
			}
		});
		node.find("input[type=password]").each(function(k,e){
			if($(e).val()){
				parameter[$(e).attr('name')]=$(e).val();
			}
		});
		node.find("input[type=hidden]").each(function(k,e){
			if($(e).val()){
				parameter[$(e).attr('name')]=$(e).val();
			}
		});
		node.find("input[type=number]").each(function(k,e){
			if($(e).val()){
				parameter[$(e).attr('name')]=$(e).val();
			}
		});
		node.find("input[type=checkbox]:checked").each(function(k,e){
	
			if($(e).val()){
					if(!parameter[$(e).attr('name')]){
						parameter[$(e).attr('name')]=$(e).val();
					}else{
						parameter[$(e).attr('name')]+=","+$(e).val();
					}
				}
		});
		node.find("input[type=radio]:checked").each(function(k,e){
			if($(e).val()){
				parameter[$(e).attr('name')]=$(e).val();
			}
		});
		node.find("select").each(function(k,e){
			if($(e).val()){
				parameter[$(e).attr('name')]=$(e).val();
			}
		});
		node.find("textarea").each(function(k,e){
			if($(e).val()){
				parameter[$(e).attr('name')]=$(e).val();
			}
		});
	
		return parameter;
	},
	/*验证不为空*/
	validateNotNull:function(val){
		if(val==0)return true;
		if(val){
			return true;
		}else{
			return false;
		}
	},
	/*表单赋值*/
	insertIntoForm:function(form,data){
		var _this=this;
		form.find("input[type=text]").each(function(k,e){
			if(_this.validateNotNull(data[$(e).attr('name')])){
				$(e).val(data[$(e).attr('name')]);
			}else{
				$(e).val('');
			}
		});
		form.find("input[type=password]").each(function(k,e){
			if(_this.validateNotNull(data[$(e).attr('name')])){
				$(e).val(data[$(e).attr('name')]);
			}else{
				$(e).val('');
			}
		});
		form.find("input[type=hidden]").each(function(k,e){
			if(_this.validateNotNull(data[$(e).attr('name')])){
				$(e).val(data[$(e).attr('name')]);
			}else{
				$(e).val('');
			}
		});
		form.find("input[type=checkbox]").each(function(k,e){
			var chVal=data[$(e).attr('name')];
			if(_this.validateNotNull(chVal))
				{
				    e.checked=false;
					var chV=chVal.split(",");
					for(var i=0;i<chV.length;i++){
						if(e.value==chV[i]){
							e.checked=true;
							$(e).parent().attr('class','checked');
							break;
						}else{
							$(e).parent().removeAttr('class');
						}
					}
				}
			
		});
		form.find("input[type=radio]").each(function(k,e){
		
			if($(e).val()==data[$(e).attr('name')]){
				e.checked=true;
				$(e).parent().attr('class','checked');
			}else{
				if(!e.checked){
					e.checked=false;
					$(e).parent().removeAttr('class');
				}
				
			}
			
		});
		form.find("select").each(function(k,e){
			var options=eval('('+$(e).attr('data-options')+')');
			if(options){
				options.defaultValue=data[$(e).attr('name')];
				$(e).attr('data-options',JSON.stringify(options));
			}else{
				if(data[$(e).attr('name')]){
					$(e).val(data[$(e).attr('name')]);
				}
			}
		});
		form.find("textarea").each(function(k,e){
			if($(e).attr('name')){
				if(_this.validateNotNull(data[$(e).attr('name')]))
					{
						var dataOption=$(e).attr('data-options');
						if(!dataOption){
							$(e).val(data[$(e).attr('name')]);
							return;
						}
						var json=eval('('+dataOption+')');
						if(json&&json.formatter){
							var s=json.formatter(data[$(e).attr('name')]);
							$(e).val(s);
						}else if(json&&json.replace){
							var str=_this.getReplaceValue(json,data[$(e).attr('name')]);
							$(e).val(str);
						}else{
							$(e).val(data[$(e).attr('name')]);
						}
					}else{
						$(e).val('');
					}
			}
		});
		form.find("span").each(function(k,e){
			if($(e).attr('name')){
				if(_this.validateNotNull(data[$(e).attr('name')]))
					{
						var dataOption=$(e).attr('data-options');
						if(!dataOption){
							$(e).text(data[$(e).attr('name')]);
							return;
						}
						var json=eval('('+dataOption+')');
						if(json&&json.formatter){
							var s=json.formatter(data[$(e).attr('name')]);
							$(e).text(s);
						}else if(json&&json.replace){
							var str=_this.getReplaceValue(json,data[$(e).attr('name')]);
							$(e).text(str);
						}else{
							$(e).text(data[$(e).attr('name')]);
						}
					}else{
						$(e).text('');
					}
			}
		});
		form.find("label").each(function(k,e){
			if($(e).attr('name')){
				if(_this.validateNotNull(data[$(e).attr('name')]))
				{
				$(e).text(data[$(e).attr('name')]);
				}else{
					$(e).text('');
				}
			}
		});
		form.find("img").each(function(k,e){
			if(_this.validateNotNull(data[$(e).attr('name')])){
				$(e).attr('src',data[$(e).attr('name')]);
			}else{
				$(e).val('');
			}
		});
	}
};

function formatNull(data) {
	if(data=='undefined') {
		return '';
	}
	return (data==null?'':data);
}

function formatMoney(data) {
	if(data=='undefined') {
		return '';
	}
	var f = parseFloat(data);
	if (isNaN(f)) { 
		return ''; 
	} 
	f = f/1000;
	return f;
}

function formatUnixtime(unixtime) {
	if(unixtime=='undefined') {
		return '';
	}
	if(unixtime==''||unixtime==null) {
		return '';
	}
	var date = new Date(parseInt(unixtime)*1000);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    var second = date.getSeconds();
    minute = minute < 10 ? ('0' + minute) : minute;
    second = second < 10 ? ('0' + second) : second;
    return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
}

function validateCheckbox(name) {
	var CheckBox = $('input[name = '+name+']');//得到所的复选框
	var SelectFalse = false; 
	for(var i = 0; i < CheckBox.length; i++) {
		if(CheckBox[i].checked) {
			SelectFalse = true;
		}
	}
	return SelectFalse;
}

function iframeCallback(form, callback){
	var $form = $(form), $iframe = $("#callbackframe");
	if(!$form.valid()) {return false;}

	if ($iframe.size() == 0) {
		$iframe = $("<iframe id='callbackframe' name='callbackframe' src='about:blank' style='display:none'></iframe>").appendTo("body");
	}
//if(!form.ajax) {
		//$form.append('<input type="hidden" name="ajax" value="1" />');
	//}
	form.target = "callbackframe";
	
	_iframeResponse($iframe[0], callback);
}







