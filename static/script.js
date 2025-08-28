// URL Shortener Frontend JavaScript

class UrlShortener {
    constructor() {
        this.baseUrl = '/api/v1';
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadRecentUrls();
    }

    bindEvents() {
        // Form submission
        document.getElementById('shortenForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.shortenUrl();
        });

        // Copy button
        document.getElementById('copyBtn').addEventListener('click', () => {
            this.copyToClipboard();
        });

        // Test link button
        document.getElementById('testLinkBtn').addEventListener('click', () => {
            this.testLink();
        });

        // View stats button
        document.getElementById('viewStatsBtn').addEventListener('click', () => {
            this.viewUrlStats();
        });

        // Stats modal
        document.getElementById('statsBtn').addEventListener('click', () => {
            this.showStatsModal();
        });

        document.getElementById('closeStatsModal').addEventListener('click', () => {
            this.hideModal('statsModal');
        });

        // Admin modal
        document.getElementById('adminBtn').addEventListener('click', () => {
            this.showAdminModal();
        });

        document.getElementById('closeAdminModal').addEventListener('click', () => {
            this.hideModal('adminModal');
        });

        document.getElementById('cleanupBtn').addEventListener('click', () => {
            this.cleanupExpiredUrls();
        });

        document.getElementById('refreshAdminBtn').addEventListener('click', () => {
            this.loadAdminData();
        });

        // Close modals when clicking outside
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('fixed')) {
                this.hideModal('statsModal');
                this.hideModal('adminModal');
            }
        });
    }

    async shortenUrl() {
        const form = document.getElementById('shortenForm');
        const formData = new FormData(form);
        
        const requestData = {
            url: formData.get('longUrl'),
            customAlias: formData.get('customAlias') || null,
            expiresAt: formData.get('expiresAt') || null
        };

        this.showLoading();

        try {
            const response = await fetch(`${this.baseUrl}/shorten`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            this.displayResult(result);
            this.loadRecentUrls();
            this.showToast('短链接生成成功！', 'success');
        } catch (error) {
            console.error('Error:', error);
            this.showToast('生成短链接失败，请重试', 'error');
        } finally {
            this.hideLoading();
        }
    }

    displayResult(result) {
        document.getElementById('shortUrl').textContent = result.shortUrl;
        document.getElementById('originalUrl').textContent = result.longUrl;
        document.getElementById('createdAt').textContent = new Date(result.createdAt).toLocaleString('zh-CN');
        
        document.getElementById('resultSection').classList.remove('hidden');
        document.getElementById('resultSection').scrollIntoView({ behavior: 'smooth' });
    }

    async copyToClipboard() {
        const shortUrl = document.getElementById('shortUrl').textContent;
        try {
            await navigator.clipboard.writeText(shortUrl);
            this.showToast('链接已复制到剪贴板', 'success');
        } catch (error) {
            // Fallback for older browsers
            const textArea = document.createElement('textarea');
            textArea.value = shortUrl;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand('copy');
            document.body.removeChild(textArea);
            this.showToast('链接已复制到剪贴板', 'success');
        }
    }

    testLink() {
        const shortUrl = document.getElementById('shortUrl').textContent;
        window.open(shortUrl, '_blank');
    }

    async viewUrlStats() {
        const shortUrl = document.getElementById('shortUrl').textContent;
        const shortCode = shortUrl.split('/').pop();
        
        try {
            const response = await fetch(`${this.baseUrl}/stats/${shortCode}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const stats = await response.json();
            this.displayUrlStats(stats);
        } catch (error) {
            console.error('Error:', error);
            this.showToast('获取统计信息失败', 'error');
        }
    }

    displayUrlStats(stats) {
        const modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
        modal.innerHTML = `
            <div class="bg-white rounded-xl p-8 max-w-md w-full mx-4">
                <div class="flex justify-between items-center mb-6">
                    <h3 class="text-xl font-bold text-gray-800">链接统计</h3>
                    <button class="text-gray-500 hover:text-gray-700" onclick="this.closest('.fixed').remove()">
                        <i class="fas fa-times text-xl"></i>
                    </button>
                </div>
                <div class="space-y-4">
                    <div class="flex justify-between">
                        <span class="text-gray-600">短链接:</span>
                        <span class="font-mono">${stats.shortUrl}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-gray-600">点击次数:</span>
                        <span class="font-bold text-blue-600">${stats.clickCount}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-gray-600">创建时间:</span>
                        <span>${new Date(stats.createdAt).toLocaleString('zh-CN')}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-gray-600">状态:</span>
                        <span class="px-2 py-1 rounded text-sm ${stats.expired ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'}">
                            ${stats.expired ? '已过期' : '有效'}
                        </span>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }

    async loadRecentUrls() {
        try {
            const response = await fetch(`${this.baseUrl}/admin/urls`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const urls = await response.json();
            this.displayRecentUrls(urls.slice(0, 5)); // Show only recent 5
        } catch (error) {
            console.error('Error loading recent URLs:', error);
        }
    }

    displayRecentUrls(urls) {
        const container = document.getElementById('recentUrls');
        
        if (urls.length === 0) {
            container.innerHTML = '<p class="text-gray-500 text-center py-4">暂无链接记录</p>';
            return;
        }

        container.innerHTML = urls.map(url => `
            <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                <div class="flex-1 min-w-0">
                    <div class="flex items-center space-x-3">
                        <span class="font-mono text-blue-600">${url.shortUrl}</span>
                        <i class="fas fa-arrow-right text-gray-400"></i>
                        <span class="text-gray-800 truncate">${url.longUrl}</span>
                    </div>
                    <div class="flex items-center space-x-4 mt-2 text-sm text-gray-500">
                        <span><i class="fas fa-mouse-pointer mr-1"></i>${url.clickCount} 次点击</span>
                        <span><i class="fas fa-calendar mr-1"></i>${new Date(url.createdAt).toLocaleDateString('zh-CN')}</span>
                    </div>
                </div>
                <div class="flex items-center space-x-2">
                    <button onclick="urlShortener.copyUrl('${window.location.origin}/api/v1/${url.shortUrl}')" 
                            class="text-blue-500 hover:text-blue-700 p-2">
                        <i class="fas fa-copy"></i>
                    </button>
                    <button onclick="urlShortener.deleteUrl('${url.shortUrl}')" 
                            class="text-red-500 hover:text-red-700 p-2">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `).join('');
    }

    async copyUrl(url) {
        try {
            await navigator.clipboard.writeText(url);
            this.showToast('链接已复制', 'success');
        } catch (error) {
            console.error('Copy failed:', error);
        }
    }

    async deleteUrl(shortUrl) {
        if (!confirm('确定要删除这个短链接吗？')) {
            return;
        }

        try {
            const response = await fetch(`${this.baseUrl}/admin/urls/${shortUrl}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.showToast('链接已删除', 'success');
            this.loadRecentUrls();
        } catch (error) {
            console.error('Error:', error);
            this.showToast('删除失败', 'error');
        }
    }

    async showStatsModal() {
        this.showModal('statsModal');
        await this.loadSystemStats();
    }

    async loadSystemStats() {
        try {
            const response = await fetch(`${this.baseUrl}/admin/stats`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const stats = await response.json();
            this.displaySystemStats(stats);
        } catch (error) {
            console.error('Error:', error);
            document.getElementById('statsContent').innerHTML = '<p class="text-red-500">加载统计信息失败</p>';
        }
    }

    displaySystemStats(stats) {
        document.getElementById('statsContent').innerHTML = `
            <div class="bg-blue-50 p-6 rounded-lg text-center">
                <div class="text-3xl font-bold text-blue-600">${stats.totalUrls}</div>
                <div class="text-sm text-blue-800">总链接数</div>
            </div>
            <div class="bg-green-50 p-6 rounded-lg text-center">
                <div class="text-3xl font-bold text-green-600">${stats.totalClicks}</div>
                <div class="text-sm text-green-800">总点击数</div>
            </div>
            <div class="bg-purple-50 p-6 rounded-lg text-center">
                <div class="text-3xl font-bold text-purple-600">${stats.urlsCreatedToday}</div>
                <div class="text-sm text-purple-800">今日新增</div>
            </div>
        `;
    }

    async showAdminModal() {
        this.showModal('adminModal');
        await this.loadAdminData();
    }

    async loadAdminData() {
        try {
            const response = await fetch(`${this.baseUrl}/admin/urls`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const urls = await response.json();
            this.displayAdminData(urls);
        } catch (error) {
            console.error('Error:', error);
            document.getElementById('adminContent').innerHTML = '<p class="text-red-500">加载数据失败</p>';
        }
    }

    displayAdminData(urls) {
        const content = document.getElementById('adminContent');
        
        if (urls.length === 0) {
            content.innerHTML = '<p class="text-gray-500 text-center py-4">暂无数据</p>';
            return;
        }

        content.innerHTML = `
            <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">短链接</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">原始链接</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">点击数</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        ${urls.map(url => `
                            <tr>
                                <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-blue-600">${url.shortUrl}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 max-w-xs truncate">${url.longUrl}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${url.clickCount}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${new Date(url.createdAt).toLocaleDateString('zh-CN')}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                    <button onclick="urlShortener.deleteUrl('${url.shortUrl}')" class="text-red-600 hover:text-red-900">删除</button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    }

    async cleanupExpiredUrls() {
        if (!confirm('确定要清理所有过期的链接吗？')) {
            return;
        }

        this.showLoading();

        try {
            const response = await fetch(`${this.baseUrl}/admin/cleanup`, {
                method: 'POST'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            this.showToast(`已清理 ${result.deletedCount} 个过期链接`, 'success');
            this.loadAdminData();
        } catch (error) {
            console.error('Error:', error);
            this.showToast('清理失败', 'error');
        } finally {
            this.hideLoading();
        }
    }

    showModal(modalId) {
        document.getElementById(modalId).classList.remove('hidden');
    }

    hideModal(modalId) {
        document.getElementById(modalId).classList.add('hidden');
    }

    showLoading() {
        document.getElementById('loading').classList.remove('hidden');
    }

    hideLoading() {
        document.getElementById('loading').classList.add('hidden');
    }

    showToast(message, type = 'success') {
        const toast = document.getElementById('toast');
        const toastMessage = document.getElementById('toastMessage');
        
        toastMessage.textContent = message;
        
        // Set color based on type
        if (type === 'error') {
            toast.className = toast.className.replace('bg-green-500', 'bg-red-500');
        } else {
            toast.className = toast.className.replace('bg-red-500', 'bg-green-500');
        }
        
        toast.classList.remove('hidden');
        
        setTimeout(() => {
            toast.classList.add('hidden');
        }, 3000);
    }
}

// Initialize the application
const urlShortener = new UrlShortener();