import Vue from 'vue'
import Router from 'vue-router'
import UserManagement from '@/components/UserManagement'
import LoanApplication from '@/components/LoanApplication'
import LoanReview from '@/components/LoanReview'
import RepaymentManagement from '@/components/RepaymentManagement'
import ReminderManagement from '@/components/ReminderManagement'
import Dashboard from '@/components/Dashboard'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: Dashboard
    },
    {
      path: '/users',
      name: 'UserManagement',
      component: UserManagement
    },
    {
      path: '/loans/apply',
      name: 'LoanApplication',
      component: LoanApplication
    },
    {
      path: '/loans/review',
      name: 'LoanReview',
      component: LoanReview
    },
    {
      path: '/repayments',
      name: 'RepaymentManagement',
      component: RepaymentManagement
    },
    {
      path: '/reminders',
      name: 'ReminderManagement',
      component: ReminderManagement
    }
  ]
})