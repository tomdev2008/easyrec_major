<%@ taglib prefix="easyrec" uri="/WEB-INF/tagLib.tld" %>
<%--
  ~ Copyright 2011 Research Studios Austria Forschungsgesellschaft mBH
  ~
  ~ This file is part of easyrec.
  ~
  ~ easyrec is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ easyrec is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with easyrec.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<h1>Import Cluster Content</h1>

<p>
    Here you can import content for existing clusters. Read the
    <a href="https://sourceforge.net/p/easyrec/wiki/Cluster/#csv-import" target="_blank">Cluster CSV import guide</a>
    to get started.
</p>

<div id="uploadForm">
    <form method='post' action='${webappPath}/clustermanager/clusterimport' enctype='multipart/form-data'>
        <input type="hidden" name="tenantId" value="${tenantId}"/>
        <input id="fileSelect" type='file' name='file'/>
        <input id="fileSubmit" type='submit' value='Upload' class="button--filled easyrecblue"/>
    </form>
</div>