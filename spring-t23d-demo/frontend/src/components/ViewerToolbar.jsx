function Icon({ path, children }) {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      {path ? <path d={path} /> : children}
    </svg>
  )
}

const ICONS = {
  menu: 'M4 6h16M4 12h16M4 18h16',
  close: 'M6 6l12 12M18 6L6 18',
  reset: 'M4 4v6h6M20 20v-6h-6M4.5 10a8 8 0 0 1 14.6-3.5M19.5 14a8 8 0 0 1-14.6 3.5',
  wireframe: 'M3 8l9-5 9 5-9 5-9-5zM3 8v8l9 5 9-5V8M12 13v8',
  sun: 'M12 17a5 5 0 100-10 5 5 0 000 10zM12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42',
  moon: 'M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z',
  fullscreenEnter: 'M8 3H5a2 2 0 0 0-2 2v3M16 3h3a2 2 0 0 1 2 2v3M8 21H5a2 2 0 0 1-2-2v-3M16 21h3a2 2 0 0 0 2-2v-3',
  fullscreenExit: 'M9 3v3a2 2 0 0 1-2 2H4M15 3v3a2 2 0 0 0 2 2h3M9 21v-3a2 2 0 0 0-2-2H4M15 21v-3a2 2 0 0 1 2-2h3',
  download: 'M12 3v12m0 0l-4-4m4 4l4-4M4 21h16',
  info: 'M12 22a10 10 0 100-20 10 10 0 000 20zM12 16v-4M12 8h.01',
  camera: 'M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2zM12 17a4 4 0 100-8 4 4 0 000 8z',
  grid: 'M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z',
}

function ZoomIcon({ direction }) {
  return (
    <Icon>
      <circle cx="10" cy="10" r="6.5" />
      <path d="M15 15l5 5" />
      <path d="M7.5 10h5" />
      {direction === 'in' && <path d="M10 7.5v5" />}
    </Icon>
  )
}

function ToolbarButton({ label, onClick, disabled, active, children }) {
  return (
    <button
      type="button"
      className={`viewer-toolbar-btn${active ? ' active' : ''}`}
      onClick={onClick}
      disabled={disabled}
      title={label}
      aria-label={label}
    >
      {children}
    </button>
  )
}

function ViewerToolbar({
  visible,
  onToggleVisible,
  onZoomIn,
  onZoomOut,
  onReset,
  wireframe,
  onToggleWireframe,
  background,
  onToggleBackground,
  showGrid,
  onToggleGrid,
  showDimensions,
  onToggleDimensions,
  onScreenshot,
  fullscreen,
  onToggleFullscreen,
  downloadUrl,
}) {
  return (
    <div className="viewer-toolbar-wrap">
      <ToolbarButton label={visible ? 'Werkzeugleiste ausblenden' : 'Werkzeugleiste einblenden'} onClick={onToggleVisible}>
        <Icon path={visible ? ICONS.close : ICONS.menu} />
      </ToolbarButton>

      {visible && (
        <div className="viewer-toolbar">
          <ToolbarButton label="Vergroessern" onClick={onZoomIn}>
            <ZoomIcon direction="in" />
          </ToolbarButton>
          <ToolbarButton label="Verkleinern" onClick={onZoomOut}>
            <ZoomIcon direction="out" />
          </ToolbarButton>
          <ToolbarButton label="Ansicht zuruecksetzen" onClick={onReset}>
            <Icon path={ICONS.reset} />
          </ToolbarButton>
          <div className="viewer-toolbar-sep" />
          <ToolbarButton label="Drahtgitter umschalten" onClick={onToggleWireframe} active={wireframe}>
            <Icon path={ICONS.wireframe} />
          </ToolbarButton>
          <ToolbarButton
            label={background === 'dark' ? 'Heller Hintergrund' : 'Dunkler Hintergrund'}
            onClick={onToggleBackground}
          >
            <Icon path={background === 'dark' ? ICONS.sun : ICONS.moon} />
          </ToolbarButton>
          <ToolbarButton
            label={showGrid ? 'Gitter ausblenden' : 'Gitter anzeigen'}
            onClick={onToggleGrid}
            active={showGrid}
          >
            <Icon path={ICONS.grid} />
          </ToolbarButton>
          <ToolbarButton
            label={showDimensions ? 'Maße ausblenden' : 'Maße anzeigen'}
            onClick={onToggleDimensions}
            active={showDimensions}
          >
            <Icon path={ICONS.info} />
          </ToolbarButton>
          <ToolbarButton label={fullscreen ? 'Vollbild verlassen' : 'Vollbild'} onClick={onToggleFullscreen} active={fullscreen}>
            <Icon path={fullscreen ? ICONS.fullscreenExit : ICONS.fullscreenEnter} />
          </ToolbarButton>
          <div className="viewer-toolbar-sep" />
          <ToolbarButton label="Screenshot speichern" onClick={onScreenshot}>
            <Icon path={ICONS.camera} />
          </ToolbarButton>
          <a
            className={`viewer-toolbar-btn${!downloadUrl ? ' disabled' : ''}`}
            href={downloadUrl || undefined}
            download
            title="STL herunterladen"
            aria-label="STL herunterladen"
            onClick={(event) => !downloadUrl && event.preventDefault()}
          >
            <Icon path={ICONS.download} />
          </a>
        </div>
      )}
    </div>
  )
}

export default ViewerToolbar
