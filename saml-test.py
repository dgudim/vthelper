#!/usr/bin/env -S uv run --script

# /// script
# dependencies = [
#   "requests"
# ]
# ///

from requests.utils import cookiejar_from_dict
from requests.utils import dict_from_cookiejar
import json
from pathlib import Path
from dataclasses import dataclass
from requests.cookies import RequestsCookieJar
import requests
import re

saved_cookies: RequestsCookieJar = RequestsCookieJar()
cookies_path = Path("cookies.json")

moodle_base_url = "https://moodle.vilniustech.lt/"
moodle_saml_endpoint = (
    f"{moodle_base_url.strip('/')}/auth/saml2/sp/saml2-acs.php/{moodle_base_url.replace('https://', '').strip('/')}"
)


@dataclass
class LoginResult:
    raw_status_code: int
    raw_content: str
    is_error: bool


userid = ""
userpass = ""

login_data = {
    "UserName": f"university\\{userid}",
    "Kmsi": "true",  # 'Remember session', does not seem to do anything though
    "AuthMethod": "FormsAuthentication",
    "Password": userpass,
}


def extract_saml_response(html_str: str) -> str:
    # Extract value of SAMLResponse
    match = re.match(
        '.*?<input type="hidden" name="SAMLResponse" value="(.*?)"',
        html_str,
        flags=re.MULTILINE | re.DOTALL,
    )

    if match is None:
        return None

    saml_response = match.group(1).strip()  # Some base64 xml
    return saml_response


def extract_context(html_str: str) -> str:
    # Extract context for MFA (AzureMfaAuthentication)
    match = re.match(
        '.*?<input id="context" type="hidden" name="Context" value="(.*?)"',
        html_str,
        flags=re.MULTILINE | re.DOTALL,
    )

    if match is None:
        return None

    mfa_request_context = match.group(1).strip()  # Some base62-encoded binary sheesh
    return mfa_request_context


base_headers = {
    "User-Agent": "Mozilla/5.0 (X11; Linux x86_64; rv:148.0) Gecko/20100101 Firefox/148.0",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
    "DNT": "1",
    "Sec-GPC": "1",
    "Connection": "keep-alive",
    "Upgrade-Insecure-Requests": "1",
    "Sec-Fetch-Dest": "document",
    "Sec-Fetch-Mode": "navigate",
    "Sec-Fetch-User": "?1",
    "Priority": "u=0, i",
    "Pragma": "no-cache",
    "Cache-Control": "no-cache"
}


def login_if_needed(cookies: RequestsCookieJar):
    # Try loading the page directly
    initial_headers = {
        "Sec-Fetch-Site": "none",
        **base_headers
    }
    print("Sent initial load request")
    initial_response = requests.get(moodle_base_url, headers=initial_headers, cookies=cookies, allow_redirects=False)
    print(f"Initial: {initial_response.status_code}")

    # No login required
    if initial_response.status_code == 200:
        return LoginResult(initial_response.status_code, initial_response.content, False)

    # We got some error
    if initial_response.status_code != 303:
        return LoginResult(initial_response.status_code, initial_response.content, True)

    # We got redirected
    print("Updated cookies")
    cookies.update(initial_response.cookies)  # Here we set some moodle cookies (MDL_SSP_SessID and MoodleSession)
    redir_location = initial_response.headers["Location"]

    print(f"Redirecting to {redir_location[:100]}...")

    # TODO: Handle redirect to SAML request, but without the login, just session refresh

    # Redirect, but not to auth
    if str(redir_location).strip("/") == f"{moodle_base_url.strip('/')}/my":
        print("Initial: 303 to /my")
        return LoginResult(initial_response.status_code, initial_response.content, False)

    # Load the redirected page to emulate how browser does it
    dummy_load_response = requests.get(redir_location, headers=initial_headers, cookies=cookies, allow_redirects=False)

    print("Dummy GET")

    if dummy_load_response.status_code != 200:
        return LoginResult(dummy_load_response.status_code, dummy_load_response.content, True)

    # This doesn't set anything, bu whatever, update if it does in the future
    cookies.update(dummy_load_response.cookies)

    print("Dummy GET: 200")

    login_headers = {
        "Content-Type": "application/x-www-form-urlencoded",
        "Origin": "https://fs.vilniustech.lt",
        "Referer": redir_location,
        "Sec-Fetch-Site": "same-origin",
        **base_headers
    }

    # Post the login data
    login_response = requests.post(
        redir_location, headers=login_headers, cookies=cookies, data=login_data, allow_redirects=False
    )

    print("Posted login details")

    # We got some error, we expect a redirection at this step to complete the 2FA
    if login_response.status_code != 302:
        print(f"Login response: {login_response.status_code}")
        return LoginResult(login_response.status_code, login_response.content, True)

    print("Updated cookies")
    cookies.update(login_response.cookies)  # Sets the MSISAuth cookie
    redir_location = login_response.headers[
        "Location"
    ]  # This should in theory be the exact same thing, but set just in case something changes in teh parameters

    login_headers["Referer"] = redir_location

    print("Loading redirection page for MFA and extracting context")

    # Load the redirected page to emulate how browser does it
    dummy_load_response = requests.get(redir_location, headers=login_headers, cookies=cookies, allow_redirects=False)

    if dummy_load_response.status_code != 200:
        return LoginResult(dummy_load_response.status_code, dummy_load_response.content, True)

    mfa_request_context = extract_context(dummy_load_response.content.decode("utf-8"))
    if mfa_request_context is None:
        return LoginResult(dummy_load_response.status_code, dummy_load_response.content, True)

    # This doesn't set anything, bu whatever, update if it does in the future
    cookies.update(dummy_load_response.cookies)

    mfa_initial_response = requests.post(
        redir_location,
        headers=login_headers,
        cookies=cookies,
        allow_redirects=False,
        data={"AuthMethod": "AzureMfaAuthentication", "Context": mfa_request_context, "__EVENTTARGET": ""},
    )

    if mfa_initial_response.status_code != 200:
        return LoginResult(mfa_initial_response.status_code, mfa_initial_response.content, True)

    # This doesn't set anything, bu whatever, update if it does in the future
    cookies.update(mfa_initial_response.cookies)

    print("Posted initial MFA request, loading the page and requesting MFA code")

    # Extract the updated context
    mfa_request_context = extract_context(mfa_initial_response.content.decode("utf-8"))
    if mfa_request_context is None:
        return LoginResult(mfa_initial_response.status_code, mfa_initial_response.content, True)

    _2fa_code = input("Please input 2fa code: ").strip()

    mfa_code_response = requests.post(
        redir_location,
        headers=login_headers,
        cookies=cookies,
        allow_redirects=False,
        data={
            "AuthMethod": "AzureMfaAuthentication",
            "Context": mfa_request_context,
            "__EVENTTARGET": "",
            "VerificationCode": _2fa_code,
            "SignIn": "Sign in",
        },
    )

    # We should get a 302, if it's 200 it's actually an error lol
    if mfa_code_response.status_code != 302:
        return LoginResult(mfa_code_response.status_code, mfa_code_response.content, True)

    print("Got MFA code response")

    cookies.update(mfa_code_response.cookies)  # this resets MSISAuth cookie and sets the MSISAuth1 cookie
    redir_location = login_response.headers[
        "Location"
    ]  # This should in theory be the exact same thing, but set just in case something changes in the parameters

    # Load the redirected page to emulate how browser does it
    dummy_load_response = requests.get(redir_location, headers=login_headers, cookies=cookies, allow_redirects=False)

    if dummy_load_response.status_code != 200:
        return LoginResult(dummy_load_response.status_code, dummy_load_response.content, True)

    print("Extracting SAML response and authenticating to moodle")

    # We get a hidden form in the response with some stuff to post to moodle
    saml_response = extract_saml_response(dummy_load_response.content.decode("utf-8"))
    if saml_response is None:
        return LoginResult(dummy_load_response.status_code, dummy_load_response.content, True)

    # This sets a bunch of cookies, including SamlSession and MSISAuthenticated and clears MSISAuth1 cookie
    cookies.update(dummy_load_response.cookies)

    login_headers["Referer"] = login_headers["Origin"]  # https://fs.vilniustech.lt, needed for moodle saml auth request

    moodle_saml_auth_data = {"SAMLResponse": saml_response, "RelayState": f"{moodle_base_url.strip('/')}/"}

    moodle_saml_response = requests.post(
        moodle_saml_endpoint, headers=login_headers, cookies=cookies, allow_redirects=False, data=moodle_saml_auth_data
    )

    print("Posted saml to moodle")

    if moodle_saml_response.status_code != 303:
        return LoginResult(moodle_saml_response.status_code, moodle_saml_response.content, True)

    redir_location = moodle_saml_response.headers["Location"]  # This should return the moodle base url

    # This sets MDL_SSP_AuthToken
    cookies.update(moodle_saml_response.cookies)

    # One last request and we should be golden
    moodle_load_response = requests.get(redir_location, headers=login_headers, cookies=cookies, allow_redirects=False)

    # Redirect is ok, we are redirected to the /my page, 200 should be ok as well
    if moodle_load_response.status_code != 200 and moodle_load_response.status_code != 303:
        return LoginResult(moodle_load_response.status_code, moodle_load_response.content, True)

    print("SUCCESS!!!")

    # This sets MOODLEID1_ and MoodleSession
    cookies.update(moodle_load_response.cookies)

    cookies_path.write_text(json.dumps(dict_from_cookiejar(cookies)))

    return LoginResult(moodle_load_response.status_code, moodle_load_response.content, False)


if cookies_path.exists():
    saved_cookies = cookiejar_from_dict(json.loads(cookies_path.read_text()))
login_if_needed(saved_cookies)
