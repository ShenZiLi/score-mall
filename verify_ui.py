"""
积分商城全栈 UI 验证脚本
验证 App 端和后管平台的页面渲染与关键流程
"""
from playwright.sync_api import sync_playwright
import sys, os

CHROME_PATH = "/root/.cache/ms-playwright/chromium-1124/chrome-linux/chrome"

def log(msg):
    print(f"[TEST] {msg}", flush=True)

def test_app(p):
    """测试 App 端"""
    log("========== App 端测试 ==========")
    browser = p.chromium.launch(headless=True, executable_path=CHROME_PATH)
    # 移动端视口
    context = browser.new_context(viewport={"width": 375, "height": 812}, user_agent="Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)")
    page = context.new_page()
    errors = []
    page.on("pageerror", lambda e: errors.append(str(e)))
    page.on("console", lambda m: errors.append(f"console.{m.type}: {m.text}") if m.type in ("error",) else None)

    try:
        # 1. 首页（公开）
        log("1. 访问首页 /home")
        page.goto("http://localhost:5173/home", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(1000)
        page.screenshot(path="/workspace/verify-app-home.png", full_page=False)
        # 检查关键元素
        body_text = page.inner_text("body")
        has_banner = "积分商城" in body_text or page.locator(".van-swipe, .van-image").count() > 0
        log(f"   首页渲染: banners/images 存在={has_banner}, body长度={len(body_text)}")

        # 2. 商品列表
        log("2. 访问商品列表 /products")
        page.goto("http://localhost:5173/products", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(800)
        page.screenshot(path="/workspace/verify-app-products.png", full_page=False)
        log(f"   商品页 body长度={len(page.inner_text('body'))}")

        # 3. 商品详情
        log("3. 访问商品详情 /product/1")
        page.goto("http://localhost:5173/product/1", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(800)
        page.screenshot(path="/workspace/verify-app-product-detail.png", full_page=False)
        detail_text = page.inner_text("body")
        log(f"   详情页含商品信息={'无线蓝牙耳机' in detail_text or '积分' in detail_text}")

        # 4. 登录页
        log("4. 访问登录页 /login")
        page.goto("http://localhost:5173/login", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(800)
        page.screenshot(path="/workspace/verify-app-login.png", full_page=False)
        login_text = page.inner_text("body")
        log(f"   登录页含登录按钮={'登录' in login_text}")

        # 5. 执行登录流程
        log("5. 执行登录 (13800000001 / 123456)")
        # 找手机号输入框
        inputs = page.locator("input")
        count = inputs.count()
        log(f"   输入框数量={count}")
        if count >= 2:
            inputs.nth(0).fill("13800000001")
            inputs.nth(1).fill("123456")
            page.wait_for_timeout(300)
            # 点登录按钮
            page.locator("button:has-text('登录'), .van-button:has-text('登录')").first.click()
            page.wait_for_timeout(2000)
            page.screenshot(path="/workspace/verify-app-after-login.png", full_page=False)
            log(f"   登录后 URL={page.url}")

        # 6. 个人中心（登录后）
        log("6. 访问个人中心 /profile")
        page.goto("http://localhost:5173/profile", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(1000)
        page.screenshot(path="/workspace/verify-app-profile.png", full_page=False)
        profile_text = page.inner_text("body")
        log(f"   个人中心含积分={'积分' in profile_text}, 含等级={'会员' in profile_text or '普通' in profile_text}")

        # 7. 签到页
        log("7. 访问签到页 /sign")
        page.goto("http://localhost:5173/sign", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(800)
        page.screenshot(path="/workspace/verify-app-sign.png", full_page=False)
        log(f"   签到页 body长度={len(page.inner_text('body'))}")

        # 8. 订单列表
        log("8. 访问订单列表 /orders")
        page.goto("http://localhost:5173/orders", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(800)
        page.screenshot(path="/workspace/verify-app-orders.png", full_page=False)
        log(f"   订单页 body长度={len(page.inner_text('body'))}")

    except Exception as e:
        log(f"   App 测试异常: {e}")
    finally:
        if errors:
            log(f"   App 页面错误({len(errors)}): {errors[:3]}")
        browser.close()

def test_admin(p):
    """测试后管平台"""
    log("========== 后管平台测试 ==========")
    browser = p.chromium.launch(headless=True, executable_path=CHROME_PATH)
    context = browser.new_context(viewport={"width": 1440, "height": 900})
    page = context.new_page()
    errors = []
    page.on("pageerror", lambda e: errors.append(str(e)))
    page.on("console", lambda m: errors.append(f"console.{m.type}: {m.text}") if m.type in ("error",) else None)

    try:
        # 1. 登录页
        log("1. 访问后管登录页 /login")
        page.goto("http://localhost:5174/login", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(1000)
        page.screenshot(path="/workspace/verify-admin-login.png", full_page=False)
        log(f"   登录页 body长度={len(page.inner_text('body'))}")

        # 2. 执行登录
        log("2. 执行登录 (admin / admin123)")
        inputs = page.locator("input")
        count = inputs.count()
        log(f"   输入框数量={count}")
        if count >= 2:
            inputs.nth(0).fill("admin")
            inputs.nth(1).fill("admin123")
            page.wait_for_timeout(300)
            page.locator("button:has-text('登 录'), button:has-text('登录'), .el-button:has-text('登')").first.click()
            page.wait_for_timeout(2500)
            page.screenshot(path="/workspace/verify-admin-dashboard.png", full_page=False)
            log(f"   登录后 URL={page.url}")

        # 3. 仪表盘
        log("3. 仪表盘检查")
        dash_text = page.inner_text("body")
        log(f"   仪表盘含统计={'用户' in dash_text or '订单' in dash_text}, 含图表={page.locator('canvas').count()>0}")

        # 4. 用户管理
        log("4. 访问用户管理 /users")
        page.goto("http://localhost:5174/users", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(1000)
        page.screenshot(path="/workspace/verify-admin-users.png", full_page=False)
        users_text = page.inner_text("body")
        log(f"   用户管理含数据={'13800000001' in users_text or '手机' in users_text}")

        # 5. 商品管理
        log("5. 访问商品管理 /products")
        page.goto("http://localhost:5174/products", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(1000)
        page.screenshot(path="/workspace/verify-admin-products.png", full_page=False)
        prod_text = page.inner_text("body")
        log(f"   商品管理含数据={'无线蓝牙耳机' in prod_text or '商品' in prod_text}")

        # 6. 订单管理
        log("6. 访问订单管理 /orders")
        page.goto("http://localhost:5174/orders", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(1000)
        page.screenshot(path="/workspace/verify-admin-orders.png", full_page=False)
        ord_text = page.inner_text("body")
        log(f"   订单管理含数据={'订单' in ord_text}")

        # 7. 分类管理
        log("7. 访问分类管理 /categories")
        page.goto("http://localhost:5174/categories", wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(800)
        page.screenshot(path="/workspace/verify-admin-categories.png", full_page=False)
        cat_text = page.inner_text("body")
        log(f"   分类管理含数据={'数码' in cat_text or '分类' in cat_text}")

    except Exception as e:
        log(f"   Admin 测试异常: {e}")
    finally:
        if errors:
            log(f"   Admin 页面错误({len(errors)}): {errors[:3]}")
        browser.close()

if __name__ == "__main__":
    with sync_playwright() as p:
        test_app(p)
        test_admin(p)
    log("========== 全部测试完成 ==========")
