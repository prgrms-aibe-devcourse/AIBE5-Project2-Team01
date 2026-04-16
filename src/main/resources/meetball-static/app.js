const MOCK_PROJECTS = [
    {
        id: 'proj1',
        title: 'AI 기반 맛집 추천 서비스 "맛있을지도"',
        description: '사용자의 취향을 분석하여 최적의 맛집을 추천해주는 서비스를 만들고 있습니다.',
        leaderNickname: '코딩왕',
        leaderAvatar: 'https://picsum.photos/seed/user1/200',
        type: 'Startup',
        techStack: ['React', 'Node.js', 'Python'],
        current: 3,
        total: 5,
        deadline: 'D-6',
        thumbnailUrl: 'https://picsum.photos/seed/food/800/400',
        isRecommended: true,
        reason: '기술 스택이 80% 일치합니다'
    },
    {
        id: 'proj2',
        title: '대학생 전용 중고 도서 거래 플랫폼',
        description: '비싼 전공 서적을 저렴하게 거래할 수 있는 캠퍼스 기반 중고 거래 앱입니다.',
        leaderNickname: '디자인요정',
        leaderAvatar: 'https://picsum.photos/seed/user2/200',
        type: 'Side Project',
        techStack: ['Flutter', 'Firebase'],
        current: 1,
        total: 4,
        deadline: 'D-31',
        thumbnailUrl: 'https://picsum.photos/seed/books/800/400',
        isRecommended: true,
        reason: '이전에 참여한 프로젝트와 유사합니다'
    },
    {
        id: 'proj3',
        title: '오픈소스 디자인 시스템 구축',
        description: '누구나 쉽게 사용할 수 있는 리액트 기반의 디자인 시스템을 구축하고 있습니다.',
        leaderNickname: '코딩왕',
        leaderAvatar: 'https://picsum.photos/seed/user1/200',
        type: 'Open Source',
        techStack: ['React', 'Storybook', 'Tailwind'],
        current: 2,
        total: 5,
        deadline: '마감임박',
        thumbnailUrl: 'https://picsum.photos/seed/design/800/400',
        isRecommended: false
    },
    {
        id: 'proj4',
        title: '환경 보호 캠페인 기부 앱 "그린포인트"',
        description: '일상 속 환경 보호 활동을 인증하고 포인트를 적립하여 기부하는 서비스입니다.',
        leaderNickname: '에코파워',
        leaderAvatar: 'https://picsum.photos/seed/user3/200',
        type: 'Startup',
        techStack: ['React Native', 'AWS'],
        current: 0,
        total: 3,
        deadline: 'D-12',
        thumbnailUrl: 'https://picsum.photos/seed/nature/800/400',
        isRecommended: false
    }
];

const projectGrid = document.getElementById('projectGrid');
const recommendedGrid = document.getElementById('recommendedGrid');
const searchInput = document.getElementById('searchInput');
const filterButtons = document.querySelectorAll('.filter-btn');
const gridViewBtn = document.getElementById('gridViewBtn');
const listViewBtn = document.getElementById('listViewBtn');
const emptyState = document.getElementById('emptyState');

let currentFilter = 'all';
let currentSearch = '';

function renderProjects() {
    const filtered = MOCK_PROJECTS.filter(proj => {
        const matchesType = currentFilter === 'all' || proj.type === currentFilter;
        const matchesSearch = proj.title.toLowerCase().includes(currentSearch.toLowerCase()) || 
                             proj.description.toLowerCase().includes(currentSearch.toLowerCase());
        return matchesType && matchesSearch;
    });

    projectGrid.innerHTML = '';
    
    if (filtered.length === 0) {
        emptyState.classList.remove('hidden');
    } else {
        emptyState.classList.add('hidden');
        filtered.forEach((proj, index) => {
            projectGrid.appendChild(createProjectCard(proj, index));
        });
    }
}

function renderRecommendations() {
    const recommended = MOCK_PROJECTS.filter(proj => proj.isRecommended);
    recommendedGrid.innerHTML = '';
    recommended.forEach((proj, index) => {
        recommendedGrid.appendChild(createProjectCard(proj, index, true));
    });
}

function createProjectCard(proj, index, isAI = false) {
    const card = document.createElement('div');
    card.className = `project-card bg-white rounded-[2rem] overflow-hidden border border-gray-100 flex flex-col group fade-in shadow-sm`;
    card.style.animationDelay = `${index * 0.1}s`;

    const progress = (proj.current / proj.total) * 100;

    card.innerHTML = `
        <div class="relative project-thumbnail overflow-hidden h-48 cursor-pointer" onclick="location.href='detail.html'">
            <img src="${proj.thumbnailUrl}" alt="${proj.title}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
            <div class="absolute top-4 left-4 flex gap-2">
                <span class="px-4 py-1.5 bg-white/90 backdrop-blur-sm rounded-full text-xs font-bold text-indigo-700 shadow-sm">${proj.type}</span>
            </div>
            <button class="absolute top-4 right-4 w-10 h-10 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center text-gray-400 hover:text-red-500 transition-colors shadow-sm" onclick="event.stopPropagation()">
                <i class="fa-regular fa-heart"></i>
            </button>
            ${isAI ? `
                <div class="absolute bottom-0 left-0 right-0 bg-indigo-600/90 backdrop-blur-sm px-4 py-2 text-white text-[10px] font-bold flex items-center gap-2">
                    <i class="fa-solid fa-sparkles text-xs"></i>
                    ${proj.reason}
                </div>
            ` : ''}
        </div>
        <div class="p-6 flex flex-col flex-grow project-content cursor-pointer" onclick="location.href='detail.html'">
            <div class="flex items-center gap-2 mb-3">
                <img src="${proj.leaderAvatar}" class="w-6 h-6 rounded-full border border-gray-100 shadow-sm">
                <span class="text-xs font-bold text-gray-500">${proj.leaderNickname}</span>
                <span class="text-[10px] text-gray-300 ml-auto font-bold">${proj.deadline}</span>
            </div>
            <h3 class="text-lg font-bold text-gray-900 mb-2 leading-tight group-hover:text-indigo-600 transition-colors line-clamp-2">${proj.title}</h3>
            <p class="text-sm text-gray-500 line-clamp-2 mb-5 leading-relaxed font-medium">${proj.description}</p>
            
            <div class="mt-auto">
                <div class="flex flex-wrap gap-2 mb-6">
                    ${proj.techStack.map(tech => `
                        <span class="px-3 py-1 bg-gray-50 text-gray-500 text-[10px] font-bold rounded-lg border border-gray-100">${tech}</span>
                    `).join('')}
                </div>
                
                <div class="flex items-center justify-between mb-2">
                    <span class="text-[11px] font-extrabold text-gray-400">모집 현황</span>
                    <span class="text-[11px] font-extrabold text-indigo-600">${proj.current} / ${proj.total}</span>
                </div>
                <div class="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden progress-bar-shine">
                    <div class="h-full bg-indigo-600 rounded-full transition-all duration-1000" style="width: ${progress}%"></div>
                </div>
            </div>
        </div>
    `;
    return card;
}

// Event Listeners
filterButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        filterButtons.forEach(b => {
            b.classList.remove('active', 'bg-indigo-600', 'text-white');
            b.classList.add('bg-white', 'text-gray-600', 'border-indigo-50');
        });
        btn.classList.add('active', 'bg-indigo-600', 'text-white');
        btn.classList.remove('bg-white', 'text-gray-600', 'border-indigo-50');
        
        currentFilter = btn.getAttribute('data-type');
        renderProjects();
    });
});

searchInput.addEventListener('input', (e) => {
    currentSearch = e.target.value;
    renderProjects();
});

gridViewBtn.addEventListener('click', () => {
    projectGrid.classList.remove('grid-cols-1', 'md:grid-cols-2', 'lg:grid-cols-3', 'xl:grid-cols-4');
    projectGrid.classList.add('grid', 'grid-cols-1', 'md:grid-cols-2', 'lg:grid-cols-3', 'xl:grid-cols-4');
    gridViewBtn.classList.add('bg-white', 'shadow-sm', 'text-indigo-600');
    listViewBtn.classList.remove('bg-white', 'shadow-sm', 'text-indigo-600');
    listViewBtn.classList.add('text-gray-400');
});

listViewBtn.addEventListener('click', () => {
    // Basic list view toggle (just changing columns for now in this static version)
    projectGrid.classList.remove('md:grid-cols-2', 'lg:grid-cols-3', 'xl:grid-cols-4');
    projectGrid.classList.add('grid-cols-1');
    listViewBtn.classList.add('bg-white', 'shadow-sm', 'text-indigo-600');
    gridViewBtn.classList.remove('bg-white', 'shadow-sm', 'text-indigo-600');
    gridViewBtn.classList.add('text-gray-400');
});

// Initial Render
renderRecommendations();
renderProjects();
