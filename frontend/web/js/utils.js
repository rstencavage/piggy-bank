
function clearAfter(id, ms = 4000) {
    const el = document.getElementById(id);
    if (!el) return;

    el.classList.add("fade");
    setTimeout(() => {
        el.classList.add("out");
        setTimeout(() => {
            el.textContent = "";
            el.classList.remove("out");
        }, 400);
    }, ms);
}