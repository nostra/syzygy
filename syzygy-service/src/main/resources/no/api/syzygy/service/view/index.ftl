<#-- @ftlvariable name="" type="no.api.syzygy.service.view.IndexView" -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>Syzygy</title>
    <style>
        article, aside, details, figcaption, figure, footer, header,
        hgroup, menu, nav, section { display: block; }
    </style>
</head>
<body>
<section>
    <table>
        <#list payloadList as payload>
        <tr>
            <td>${payload.getName()}</td>
            <td>${payload.getValue()}</td>
            <td>${payload.getHits()}</td>
        </tr>
            <#if payload.getDoc()??>
            <tr>
            <td colspan="4">
            ${payload.getDoc()!"No doc"}
            </td></tr>
            </#if>
        <tr>
            <td colspan="4">
                <pre>
<#list payload.getPath() as path>${path}</#list>
                </pre>
            </td>
        </tr>
        </#list>
    </table>



    </pre>
</section>
</body>
</html>