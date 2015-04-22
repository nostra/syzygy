<#-- @ftlvariable name="" type="no.api.syzygy.service.view.IndexView" -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>Syzygy</title>
    <style>
        table { border-collapse: collapse; border-spacing: 0; font-family: Futura, Arial, sans-serif;
            width: 100%;}
        th, thead { background: #000; color: #fff; border: 0px solid #000; }
        th:first-child { border-radius: 9px 0 0 0; }
        th:last-child { border-radius: 0 9px 0 0; }
        tr:last-child th:first-child { border-radius: 0 0 0 9px; }
        tr:last-child th:last-child { border-radius: 0 0 9px 0; }
        tr:nth-child(even) { background: #ccc; }
    </style>
</head>
<body>
<section>
    <table>
        <tr>
            <th>key</th>
            <th>value</th>
            <th>number of hits</th>
        </tr>
        <#list payloadList as payload>
        <tr>
            <td>${payload.getName()}</td>
            <td>${payload.getValue()}</td>
            <td>${payload.getHits()}
            <#if payload.getHits() == "1">hit<#else>hits</#if>
            </td>
        </tr>
            <#if payload.getDoc()??>
            <tr>
            <td colspan="4">
            ${payload.getDoc()!"No doc"}
            </td></tr>
            </#if>
        <tr>
            <td>&nbsp;</td>
            <td colspan="3">
                <code><#list payload.getPath() as path>${path} <#if path_has_next> / </#if></#list></code>
            </td>
        </tr>
        </#list>
        <tr>
            <th>&nbsp;</th>
            <th colspan="4">&nbsp;</th>
        </tr>
    </table>
</section>
</body>
</html>