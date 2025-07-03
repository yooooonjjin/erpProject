$(function () {
    // 상위 메뉴 클릭
    $('.menu01 > a').click(function () {
        const $parentLi = $(this).parent();         // 클릭한 menu01
        const $submenu = $parentLi.children('.menu02'); // 해당 submenu

        const isAlreadySelected = $parentLi.hasClass('selected'); // 현재 선택 상태인지?

        // 모두 초기화
        $('.menu01').removeClass('selected');
        $('.menu02 li').removeClass('selected');
        $('.menu02').slideUp();

        if (!isAlreadySelected) {
            // 선택된 상태가 아니었을 경우만 새로 열기
            $parentLi.addClass('selected');
            $submenu.stop().slideDown(400);

            // 하위 메뉴 첫 번째 자동 선택
            const $firstSubItem = $submenu.find('li').first();
            $firstSubItem.addClass('selected');
        }
        // 선택된 상태였으면 아무것도 하지 않음 (닫힘 + 색상 해제만 유지)
    });

    // 하위 메뉴 클릭
    $('.menu02 li a').click(function (e) {
        e.stopPropagation(); // 상위 메뉴 클릭 이벤트 막기

        // 모든 하위 항목 초기화
        $('.menu02 li').removeClass('selected');

        // 현재만 선택
        $(this).parent().addClass('selected');
    });
});
