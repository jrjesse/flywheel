import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { AppShell } from '@/components/layout/AppShell'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'Sales Automation - Dashboard',
  description: 'Premium Sales Automation Dashboard',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="pt-BR" className="antialiased h-full">
      <body className={`${inter.className} h-full`}>
        <AppShell>{children}</AppShell>
      </body>
    </html>
  )
}
