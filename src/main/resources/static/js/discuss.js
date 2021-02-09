//匿名函数 声明了 点对应的三个按钮 调用三个方法
$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});


function like(btn,entityType,entityId,entityUserId,postId) {

    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code ==0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else {
                alert(data.msg);
            }
        }
    );
}

// 置顶
function setTop() {
    $.post(
        //路径
        CONTEXT_PATH + "/discuss/top",
        //传入的参数
        {"id":$("#postId").val()},
        function(data) {
            //处理返回的json对象
            data = $.parseJSON(data);
            if(data.code == 0) {
                //置顶成功之后,把按钮设置为不可用
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                //删除成功后 跳转回主页
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}