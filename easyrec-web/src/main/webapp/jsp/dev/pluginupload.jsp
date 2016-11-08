<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="appendbody">
    <h1>Upload plugin</h1>
    <br><br>

    <form id="uploadForm" method="post" action="pluginupload.form?tenantId=${tenantId}&operatorId=${operatorId}"
          enctype="multipart/form-data">
        <input type="file" name="file"/><br>
        <a href="#" onclick="$('#uploadForm').submit()" class="button--filled easyrecblue">Upload</a>
    </form>
</div>

