# Auth tests

The server [responds with 401](- "unauthorizedWithoutToken()")
when no authorization is performed.

## Authorization URL

-   It [returns 400](- "authNoParameters()")
    when required parameters are not provided.

-   It [provides a log-in page](- "authPage()")
    when credentials are not provided.

-   It [fails authentication](- "wrongCredentials()")
    when credentials are invalid.

-   It [redirects to the redirect URI](- "adminCredentialsRedirect()")
    when credentials are valid.

-   It [responds with a code grant](- "#authCode=getAuthCode('admin', 'admin')")
    when credentials are valid and no redirect URI is provided.

## Token URL

The code grant requested above is then
[used](- "#tokens=requestTokensFromGrant(#authCode)")
to request tokens.

The tokens should be
[valid](- "validateTokens(#tokens)").

The access token may be then
[refreshed](- "#tokens2=refreshAccessToken(#tokens)").

The refreshed tokens should be
[valid](- "validateTokens(#tokens2)").

## Accounts

The *admin* account [is valid](- "#adminTokens=fullAuth('admin', 'admin')").
The *ldapadmin* account [is valid](- "fullAuth('ldapadmin', 'admin')").

The *admin* account will be
[used](- "useTokens(#adminTokens)")
from now on as the default user.
