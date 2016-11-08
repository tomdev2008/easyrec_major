

/**
 * select another page
 */
function selectPage(url, siteNumber)
{
    window.location = url + siteNumber;
}

function selectPageSearch(url, searchTerm)
{
    window.location = url + "&searchString=" + searchTerm;
}

