<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  useAgentStore,
  useChatStore,
  useLoginFlow,
} from '@phoenix/chat-shared';
import { showFailToast } from 'vant';

const router = useRouter();
const route = useRoute();
const { submit } = useLoginFlow();

const form = reactive({
  username: '',
  password: '',
});
const submitting = ref(false);
const showTerms = ref(false);
const showPrivacy = ref(false);

async function handleSubmit() {
  const username = form.username.trim();
  if (!username) {
    showFailToast('请输入账号');
    return;
  }
  submitting.value = true;
  try {
    await submit({
      username,
      password: form.password,
      remember: true,
    });
    await Promise.all([
      useAgentStore().loadAll(),
      useChatStore().loadSessions(),
    ]);
    const redirect =
      typeof route.query.redirect === 'string' ? route.query.redirect : '/chat';
    await router.replace(redirect || '/chat');
  } catch (e: any) {
    showFailToast(e?.message || '登录失败，请检查账号密码');
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="login">
    <header class="login__hero">
      <div class="login__logo">
        <img src="/imgs/logo.png" alt="Phoenix" class="login__logo-img" />
      </div>
      <div class="login__title">Phoenix 智能体助手</div>
      <div class="login__slogan">一句话，让数据为你做事</div>
    </header>

    <form class="login__form" @submit.prevent="handleSubmit">
      <label class="field">
        <span class="field__label">账号</span>
        <input
          v-model="form.username"
          class="field__input"
          type="text"
          autocomplete="username"
          inputmode="text"
          placeholder="请输入账号"
        />
      </label>
      <label class="field">
        <span class="field__label">密码</span>
        <input
          v-model="form.password"
          class="field__input"
          type="password"
          autocomplete="current-password"
          placeholder="请输入密码"
        />
      </label>

      <button type="submit" class="login__submit" :disabled="submitting">
        {{ submitting ? '登录中…' : '登录' }}
      </button>
    </form>

    <footer class="login__footer">
      登录即代表同意
      <button type="button" class="login__link" @click="showTerms = true">用户协议</button>
      与
      <button type="button" class="login__link" @click="showPrivacy = true">隐私政策</button>
    </footer>

    <Teleport to="body">
      <div v-if="showTerms" class="legal-overlay" @click.self="showTerms = false">
        <div class="legal-overlay__panel">
          <header class="legal-overlay__header">
            <h1 class="legal-overlay__title">服务条款</h1>
            <button type="button" class="legal-overlay__close" @click="showTerms = false">
              <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
                <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" fill="none" />
              </svg>
            </button>
          </header>
          <div class="legal-overlay__body">
            <div class="legal-overlay__scroll">
              <p class="legal-overlay__updated">最后更新日期：2026 年 7 月 6 日</p>

              <section>
                <p>欢迎使用 Phoenix Agent。本平台基于人工智能技术，提供多智能体协作能力，帮助你高效完成数据分析、文档处理、代码生成、报告撰写等任务。请在使用前仔细阅读以下条款，使用即视为你已充分理解并同意接受本条款的全部约束。如你不同意本条款的任何内容，请立即停止使用本平台服务。</p>
              </section>

              <section>
                <h2>一、服务说明</h2>
                <p>本平台提供多智能体协作能力，覆盖文档、数据、代码、运维等多个场景。各智能体在特定领域具有专业能力，可协同完成复杂任务。</p>
                <p>平台基于人工智能技术生成的内容仅供参考，不构成专业建议。你应结合自身判断独立决策，对因依赖平台内容而产生的后果自行承担全部责任。</p>
                <p>平台有权根据业务发展需要对服务内容、功能模块、界面设计等进行调整、变更或优化，重要变更将通过站内消息或公告方式提前告知。</p>
                <p>平台将尽合理努力保障服务的稳定性和可用性，但服务可能因定期维护、系统升级、网络故障或不可抗力等因素而暂时中断。</p>
              </section>

              <section>
                <h2>二、账号与责任</h2>
                <p>你在使用本平台服务前需要注册一个账号。注册时应提供真实、准确、完整的个人信息，并在信息发生变更时及时更新。</p>
                <p>你的账号仅限你本人使用，不得以任何形式转让、出借或授权他人使用你的账号。因账号转让或出借导致的全部后果由你自行承担。</p>
                <p>你应妥善保管账号密码及与之相关的验证信息。由你账号产生的所有操作行为均视为你本人行为，你对这些行为负全部法律责任。</p>
                <p>如你发现账号或密码存在未经授权使用的情况，应立即通知平台。平台将采取合理措施协助你保护账号安全，但不对因延迟通知造成的损失承担责任。</p>
              </section>

              <section>
                <h2>三、内容合规</h2>
                <p>你承诺在使用本平台服务时严格遵守中华人民共和国法律法规，不得利用平台从事任何违法违规活动。</p>
                <p>你不得利用平台发布、传播或存储任何违反法律法规、社会公序良俗的信息，包括但不限于涉及国家安全、社会稳定、他人合法权益或平台安全运营的内容。</p>
                <p>你不得对本平台的软件系统、算法模型进行反向工程、反编译、反汇编、破解或试图获取源代码。</p>
                <p>你不得通过自动化手段（包括但不限于爬虫、脚本、机器人等）批量访问或调用平台接口，不得对平台的基础设施或网络造成不合理或不成比例的巨大负担。</p>
                <p>如你违反上述任一条款，平台有权立即暂停或终止你的账号使用权限，删除违规内容，并保留向有关部门举报及追究法律责任的权利。</p>
              </section>

              <section>
                <h2>四、服务变更</h2>
                <p>平台可能基于业务需要对功能进行调整或升级，重要变更将通过站内消息或公告告知。</p>
                <p>平台有权在必要时对服务进行暂停或终止，包括但不限于：服务生命周期结束、业务战略调整、法律法规或政策变化等情形。终止服务前，平台将通过合理方式提前通知你。</p>
                <p>本条款的成立、生效、履行、解释及争议解决均适用中华人民共和国法律。因本条款引起的争议，双方应友好协商解决；协商不成的，任何一方均可提交至平台所在地有管辖权的人民法院诉讼解决。</p>
              </section>
            </div>
          </div>
        </div>
      </div>

      <div v-if="showPrivacy" class="legal-overlay" @click.self="showPrivacy = false">
        <div class="legal-overlay__panel">
          <header class="legal-overlay__header">
            <h1 class="legal-overlay__title">隐私协议</h1>
            <button type="button" class="legal-overlay__close" @click="showPrivacy = false">
              <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
                <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" fill="none" />
              </svg>
            </button>
          </header>
          <div class="legal-overlay__body">
            <div class="legal-overlay__scroll">
              <p class="legal-overlay__updated">最后更新日期：2026 年 7 月 6 日</p>

              <section>
                <p>我们重视并尊重每一位用户的隐私。本协议说明 Phoenix Agent 在你使用智能体服务过程中如何收集、使用、存储与保护你的信息。请在使用本平台服务前仔细阅读本协议，如你不同意本协议的任何条款，请立即停止使用平台服务。</p>
              </section>

              <section>
                <h2>一、我们收集的信息</h2>
                <p>为向你提供稳定、高效的智能体服务，我们可能在以下场景中收集必要的信息：</p>
                <p>账户登录信息：你在注册或使用平台时提供的用户名、手机号码、电子邮箱地址等用于身份识别的信息。</p>
                <p>对话内容：你与智能体之间产生的全部对话记录，包括你输入的文本、上传的文档、图片等文件内容。</p>
                <p>操作日志：你在平台上的操作行为记录，包括功能使用频率、页面访问路径、功能触发记录等。</p>
                <p>设备与网络信息：为保障服务兼容性和安全性，我们可能收集你的 IP 地址、浏览器类型、操作系统版本、设备标识符等基础信息。</p>
              </section>

              <section>
                <h2>二、信息的使用</h2>
                <p>我们收集的信息将严格限于以下用途：</p>
                <p>身份认证与会话管理：用于验证你的登录身份、维持会话状态，确保服务连续性和安全性。</p>
                <p>智能体能力调用：你的对话内容将被用于调用相应的智能体能力，以生成回复、分析数据、撰写报告等。</p>
                <p>服务优化与改进：我们可能分析服务使用趋势和对话模式，用于改进智能体的回答质量和产品体验。</p>
                <p>合规与安全：用于检测和预防欺诈、滥用及其他违法违规行为，保障平台安全运营。</p>
                <p>我们承诺不会将你的信息用于与服务无关的第三方营销或任何其他未经你授权的用途。</p>
              </section>

              <section>
                <h2>三、信息的存储与保护</h2>
                <p>我们采取业界通行的安全措施保护你的个人信息：</p>
                <p>传输加密：采用 TLS / SSL 加密协议保护数据在传输过程中的安全，防止数据被非法截获或篡改。</p>
                <p>访问控制：建立严格的权限管理体系，仅授权人员可在必要范围内访问用户数据，所有访问行为均被记录和审计。</p>
                <p>数据脱敏：对敏感字段（如手机号码、邮箱等）进行脱敏处理，降低数据泄露风险。</p>
                <p>数据存储期限：我们仅在提供服务所必需的期限内保留你的个人信息，超出期限后将进行匿名化处理或删除。</p>
                <p>数据导出与注销：你可随时通过管理员申请导出你的个人数据或注销账号，我们将在合理期限内响应你的请求。</p>
              </section>

              <section>
                <h2>四、你的权利</h2>
                <p>根据适用的法律法规，你在使用本平台服务期间享有以下权利：</p>
                <p>访问权：你有权查阅我们收集和存储的你的个人信息。</p>
                <p>更正权：如你发现你的个人信息不准确或不完整，你有权要求更正或补充。</p>
                <p>删除权：在特定情形下（如不再需要使用服务或撤回同意），你有权要求删除你的个人信息。</p>
                <p>限制处理权：你对个人信息处理有疑问或争议时，有权要求限制我们对相关信息的使用。</p>
                <p>撤回同意权：你有权随时撤回你对个人信息使用的授权同意，撤回不影响此前基于同意进行的处理的合法性。</p>
                <p>如你希望行使上述权利，请通过平台内置的联系渠道与我们取得联系，我们将在收到请求后十五个工作日内予以回复。</p>
              </section>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style lang="scss" scoped>
.login {
  display: flex;
  flex-direction: column;
  min-height: 100dvh;
  padding: calc(var(--m-safe-top) + 56px) 24px calc(var(--m-safe-bottom) + 20px);
  background: var(--m-bg);
}

.login__hero {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  margin-bottom: 40px;
}

.login__logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8eeff 100%);
  border-radius: 18px;
  box-shadow: 0 8px 20px rgb(64 121 255 / 20%);
}

.login__logo-img {
  width: 48px;
  height: 48px;
}

.login__title {
  font-size: 22px;
  font-weight: 600;
  color: var(--m-text-primary);
}

.login__slogan {
  font-size: 13px;
  color: var(--m-text-soft);
}

.login__form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.login__submit {
  height: 48px;
  margin-top: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  cursor: pointer;
  background: var(--m-brand-primary);
  border: none;
  border-radius: 14px;
}

.login__submit:disabled {
  opacity: 0.55;
}

.login__submit:active:not(:disabled) {
  opacity: 0.85;
}

.login__footer {
  margin-top: auto;
  font-size: 12px;
  color: var(--m-text-muted);
  text-align: center;
}

.login__footer a,
.login__link {
  color: var(--m-brand-primary);
  text-decoration: none;
}

.login__link {
  display: inline;
  padding: 0;
  font: inherit;
  cursor: pointer;
  background: none;
  border: none;
}

.login__link:active {
  opacity: 0.7;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field__label {
  padding-left: 4px;
  font-size: 13px;
  color: var(--m-text-soft);
}

.field__input {
  height: 48px;
  padding: 0 14px;
  font-size: 15px;
  color: var(--m-text-primary);
  outline: none;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: 14px;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.field__input::placeholder {
  color: var(--m-text-muted);
}

.field__input:focus {
  border-color: var(--m-brand-primary);
  box-shadow: 0 0 0 3px rgb(47 107 255 / 12%);
}

.legal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(0 0 0 / 50%);
}

.legal-overlay__panel {
  display: flex;
  flex-direction: column;
  width: 90%;
  height: 90%;
  background: var(--m-bg);
  border-radius: 16px;
}

.legal-overlay__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--m-border-soft);
}

.legal-overlay__title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: var(--m-text-primary);
}

.legal-overlay__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: var(--m-text-muted);
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 50%;
}

.legal-overlay__close:active {
  background: var(--m-border-soft);
}

.legal-overlay__body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.legal-overlay__scroll {
  padding: 16px 20px 32px;
  overflow-y: auto;
  height: 100%;
  -webkit-overflow-scrolling: touch;
}

.legal-overlay__scroll section {
  margin-bottom: 20px;
}

.legal-overlay__scroll h2 {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--m-text-primary);
}

.legal-overlay__scroll p {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--m-text-secondary);
}

.legal-overlay__updated {
  margin-bottom: 16px;
  font-size: 12px;
  color: var(--m-text-muted);
}
</style>
