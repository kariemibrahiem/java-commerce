The project has a broken and inconsistent context-path setup.

Current problem:
Some parts use:
http://localhost:8080/e-commerce/

Other parts use:
http://localhost:8080/e_commerce_war_exploded/

This causes:
- 404 errors
- Invalid token errors
- Frontend login failures
- APIs working in one place and failing in another
- JWT/session inconsistencies

I need you to FIX the ENTIRE project and standardize EVERYTHING to use ONLY:

http://localhost:8080/e-commerce/

==================================================
TASKS
==================================================

1. Detect and fix Tomcat deployment configuration.
- Ensure artifact/context path is:
e-commerce

NOT:
e_commerce_war_exploded

==================================================

2. Search entire project for:
e_commerce_war_exploded

Replace ALL occurrences with:
e-commerce

==================================================

3. Fix ALL frontend files:
- signin.html
- signup.html
- dashboard.html
- products pages
- cart pages
- reviews pages
- all JS files

Replace all hardcoded URLs.

==================================================

4. Create ONE global base URL:

const BASE_URL = window.location.origin + "/e-commerce";

Use it everywhere.

==================================================

5. Fix ALL fetch/axios/API calls to use:
BASE_URL + "/api/..."

==================================================

6. Fix ALL redirects and navigation links.

==================================================

7. Fix Postman collection URLs.
All endpoints must use:
http://localhost:8080/e-commerce/

==================================================

8. Rebuild and redeploy project correctly.

==================================================

9. Test all pages and APIs:
- login
- register
- products
- cart
- reviews
- dashboard
- orders

==================================================

10. Final output:
- explain root cause
- list modified files
- confirm frontend + backend now use SAME context path
- confirm no more 404 or invalid token issues
