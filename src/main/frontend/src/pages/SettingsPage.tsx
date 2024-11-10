import React from 'react'
import { Breadcrumb } from '../components/layout/Breadcrumb'

export function SettingsPage() {
  return (
    <>
      <Breadcrumb 
        items={[
          { label: 'Home', href: '/' },
          { label: 'Settings' }
        ]} 
      />
      <div className="p-6">
        <h2 className="text-2xl font-bold mb-6">Settings</h2>
        <div className="bg-white rounded-lg shadow p-4">
          <p className="text-gray-600">Settings page content will be defined in a later stage.</p>
        </div>
      </div>
    </>
  )
}
