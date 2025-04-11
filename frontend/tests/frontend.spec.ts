import { test, expect } from '@playwright/test';


test.describe('Test Cases', () => {
   test.beforeEach(async ({ page }) => {
       await page.goto('http://localhost:9083');
   });


   test('TC_001: Verify Web Application Launches Successfully on Server (LOG_001)', async ({ page }) => {
       await expect(page.locator('body')).toBeVisible();
   });


   test('TC_002: Verify Login Popup Appears on Home Page (LOG_002)', async ({ page }) => {
       await page.goto('http://localhost:9080');
       const overlay = page.locator('div.position-fixed.top-0.start-0');
       await expect(overlay).toBeVisible({ timeout: 10000 });
   });


   test('TC_003: Verify Google Authentication Button is Present (LOG_003)', async ({ page }) => {
       const googleLoginButton = page.getByRole('button', { name: 'Continue with Google' });
       await expect(googleLoginButton).toBeVisible();
       await expect(googleLoginButton).toBeEnabled();
   });


   test('TC_005: Verify \'Login as Guest\' Button is Present (LOG_005)', async ({ page }) => {
       const guestLoginButton = page.getByRole('button', { name: 'Continue as Guest' });
       await expect(guestLoginButton).toBeVisible();
       await expect(guestLoginButton).toBeEnabled();
   });


   test('TC_006: Verify Login Popup Can Be Closed (LOG_006)', async ({ page }) => {
       await page.goto('http://localhost:9080');
       const loginOverlay = page.locator('div.position-fixed.top-0.start-0');
       await expect(loginOverlay).toBeVisible({ timeout: 10000 });


       const closeButton = page.locator('div').filter({ hasText: /^Welcome!$/ }).getByRole('button');
       await closeButton.click();


       await expect(loginOverlay).toHaveCount(0, { timeout: 5000 });
       const searchBar = page.getByPlaceholder('Search quotes, authors, or themes...');
       await expect(searchBar).toBeVisible();
   });


   test('TC_007: Verify Login Popup Can Be Closed (LOG_007)', async ({ page }) => {
       await page.goto('http://localhost:9080/');
       const loginOverlay = page.locator('div.position-fixed.top-0.start-0');
       await expect(loginOverlay).toBeVisible({ timeout: 10000 });


       const closeButton = page.locator('div').filter({ hasText: /^Welcome!$/ }).getByRole('button');
       await closeButton.click();


       await expect(loginOverlay).toHaveCount(0, { timeout: 5000 });
       const searchBar = page.getByPlaceholder('Search quotes, authors, or themes...');
       await expect(searchBar).toBeVisible();
   });


   test('TC_008: Verify Login Popup Does Not Reappear for Logged-In Users (LOG_008)', async ({ page }) => {
       await page.route('http://localhost:9081/users/auth/login', route => {
           route.fulfill({
               status: 200,
               contentType: 'application/json',
               body: JSON.stringify({ success: true, user: { id: 1, name: 'Test User' } }),
           });
       });


       const googleLoginButton = page.getByRole('button', { name: 'Continue with Google' });
       await googleLoginButton.click();
       await page.goto('http://localhost:9083');
       await expect(page.locator('[data-testid="login-box"]')).not.toBeVisible();
   });


   test('TC_010: Verify Security of Login Popup (No Data Leakage) (LOG_010)', async ({ page }) => {
       const googleLoginButton = page.getByRole('button', { name: 'Continue with Google' });
       let requestOccurred = false;


       page.on('request', request => {
           if (request.url().includes('http://localhost:9081/users/auth/login')) {
               requestOccurred = true;
           }
       });


       await googleLoginButton.click();
       await expect(requestOccurred).toBe(true);
   });


test('TC_020: Verify guests cannot upload quotes (LOG_020)', async ({ page }) => {
   await page.goto('http://localhost:9080/');
   const closeButton = page.locator('div').filter({ hasText: /^Welcome!$/ }).getByRole('button');
   await closeButton.click();


   await page.getByText('Add Quote').click();
   await page.getByRole('textbox', { name: 'Enter your quote here' }).fill('test as a guest');
   await page.getByRole('textbox', { name: 'Unknown' }).fill('guest');
   await page.getByRole('button', { name: '#Willpower' }).click();
   await page.getByRole('button', { name: 'Upload' }).click();


   const modalCloseButton = page.getByRole('button', { name: '×' });
   if (await modalCloseButton.isVisible()) {
       await modalCloseButton.click();
   }


   await page.getByRole('link', { name: 'My Collection' }).click();
   const quoteNotFound = page.getByText('No quotes found.');


   await page.getByRole('button', { name: 'Used', exact: true }).click();
   await expect(quoteNotFound).toBeVisible();
   await page.getByRole('button', { name: 'Unused' }).click();
   await expect(quoteNotFound).toBeVisible();
   await page.getByRole('button', { name: 'Private Quotes' }).click();
   await expect(quoteNotFound).toBeVisible();
   await page.getByRole('button', { name: 'Uploaded Quotes' }).click();
   await expect(quoteNotFound).toBeVisible();
   await page.getByRole('button', { name: 'Bookmarked Quotes' }).click();
   await expect(quoteNotFound).toBeVisible();
});


test('TC_021: Verify guests cannot delete quotes (LOG_021)', async ({ page }) => {
   await page.goto('http://localhost:9080/');
   const closeButton = page.locator('div').filter({ hasText: /^Welcome!$/ }).getByRole('button');
   await closeButton.click();


   const quote = page.getByText('"It is never too late to be what you might have', { exact: false });
   await quote.click();
   const deleteButton = page.getByRole('button', { name: 'Delete' });
   await expect(deleteButton).toHaveCount(0);
});


test('TC_022: Verify guests cannot edit quotes (LOG_022)', async ({ page }) => {
   await page.goto('http://localhost:9080/');
   const closeButton = page.locator('div').filter({ hasText: /^Welcome!$/ }).getByRole('button');
   await closeButton.click();


   const quote = page.getByText('"It is never too late to be what you might have', { exact: false });
   await quote.click();
   const editButton = page.getByRole('button', { name: 'Edit' });
   await expect(editButton).toHaveCount(0);
});


test('TC_023: Verify guests cannot bookmark quotes (LOG_023)', async ({ page }) => {
   await page.goto('http://localhost:9080/');
   const closeButton = page
       .locator('div')
       .filter({ hasText: /^Welcome!$/ })
       .getByRole('button');
   await closeButton.click();
   const bookmarkIcon = page.getByRole('button', { name: '3' });
   await bookmarkIcon.click();
   await page.getByRole('link', { name: 'My Collection' }).click();
   const quoteNotFound = page.getByText('No quotes found.');
   const tabs = [
       'Bookmarked Quotes',
   ];
   for (const tab of tabs) {
       const button = page.getByRole('button', { name: tab, exact: true });
       if (await button.isVisible()) {
           await button.click();
           await expect(quoteNotFound).toBeVisible();
       }
   }
});






test('TC_036: Verify search bar is available on the homepage (LOG_036)', async ({ page }) => {
   await page.goto('http://localhost:9083');
   const searchBar = page.getByPlaceholder('Search quotes, authors, or themes...');
   await expect(searchBar).toBeVisible();
});


test('TC_037: Verify user can enter a search query (LOG_037)', async ({ page }) => {
   await page.goto('http://localhost:9083');
   const searchBar = page.getByPlaceholder('Search quotes, authors, or themes...');
   await searchBar.fill('Test Query');
   await expect(searchBar).toHaveValue('Test Query');
});


test('TC_051: Check if the search button is clickable (LOG_051)', async ({ page }) => {
   await page.goto('http://localhost:9083');
   const searchButton = page.getByRole('button', { name: 'Search' });
   await expect(searchButton).toBeVisible();
});


});



