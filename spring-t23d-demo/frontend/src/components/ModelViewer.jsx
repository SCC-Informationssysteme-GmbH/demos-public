import { Suspense, useEffect, useRef, useState } from 'react'
import { Canvas, useLoader, useThree } from '@react-three/fiber'
import { OrbitControls, Grid, Center } from '@react-three/drei'
import { STLLoader } from 'three/examples/jsm/loaders/STLLoader.js'
import ViewerToolbar from './ViewerToolbar'

const BACKGROUNDS = {
  dark: { scene: '#000000', gridCell: '#333333', gridSection: '#555555' },
  light: { scene: '#ffffff', gridCell: '#d0d0d0', gridSection: '#a0a0a0' },
}

function StlModel({ url, wireframe, onGeometryReady }) {
  const geometry = useLoader(STLLoader, url)

  useEffect(() => {
    geometry.computeBoundingSphere()
    geometry.computeBoundingBox()
    const box = geometry.boundingBox
    const size = box
      ? { x: box.max.x - box.min.x, y: box.max.y - box.min.y, z: box.max.z - box.min.z }
      : null
    onGeometryReady?.({ sphere: geometry.boundingSphere, size })
  }, [geometry, onGeometryReady])

  return (
    <Center>
      <mesh geometry={geometry}>
        <meshStandardMaterial color="#7fa8d9" wireframe={wireframe} metalness={0.15} roughness={0.45} />
      </mesh>
    </Center>
  )
}

function Placeholder() {
  return (
    <mesh rotation={[0.4, 0.6, 0]}>
      <boxGeometry args={[14, 14, 14]} />
      <meshStandardMaterial color="#4a5568" wireframe />
    </mesh>
  )
}

// Rueckt die Kamera so weit vom (durch <Center> stets im Ursprung liegenden)
// Modell ab, dass es unabhaengig von seiner absoluten Groesse formatfuellend
// zu sehen ist. Blickrichtung/Winkel der Kamera bleiben dabei erhalten.
function CameraFitter({ bounds, controlsRef, margin = 1.6 }) {
  const { camera } = useThree()

  useEffect(() => {
    const controls = controlsRef.current
    const sphere = bounds?.sphere
    if (!sphere || !controls || sphere.radius <= 0) {
      return
    }

    controls.target.set(0, 0, 0)
    const direction = camera.position.clone().sub(controls.target).normalize()
    const fov = (camera.fov * Math.PI) / 180
    const distance = (sphere.radius / Math.sin(fov / 2)) * margin

    camera.position.copy(controls.target).add(direction.multiplyScalar(distance))
    camera.near = Math.max(distance / 100, 0.01)
    camera.far = distance * 100
    camera.updateProjectionMatrix()
    controls.update()
    controls.saveState()
  }, [bounds, controlsRef, camera, margin])

  return null
}

// Stellt eine Screenshot-Funktion ueber triggerRef bereit, da der Canvas-Inhalt
// nur innerhalb des R3F-Kontexts (gl/scene/camera) zugreifbar ist, der
// auslösende Toolbar-Button aber ausserhalb des <Canvas> liegt.
function ScreenshotHandler({ triggerRef }) {
  const { gl, scene, camera } = useThree()

  useEffect(() => {
    triggerRef.current = () => {
      gl.render(scene, camera)
      const link = document.createElement('a')
      link.href = gl.domElement.toDataURL('image/png')
      link.download = `text-to-3d-${Date.now()}.png`
      link.click()
    }
  }, [gl, scene, camera, triggerRef])

  return null
}

function dollyCamera(controlsRef, scale) {
  const controls = controlsRef.current
  if (!controls) return
  const camera = controls.object
  const offset = camera.position.clone().sub(controls.target)
  offset.multiplyScalar(scale)
  camera.position.copy(controls.target).add(offset)
  controls.update()
}

function formatMm(value) {
  return `${value.toFixed(1)} mm`
}

function ModelViewer({ modelUrl }) {
  const controlsRef = useRef(null)
  const containerRef = useRef(null)
  const screenshotRef = useRef(null)
  const [toolbarVisible, setToolbarVisible] = useState(true)
  const [wireframe, setWireframe] = useState(false)
  const [background, setBackground] = useState('light')
  const [showGrid, setShowGrid] = useState(true)
  const [showDimensions, setShowDimensions] = useState(true)
  const [bounds, setBounds] = useState(null)
  const [fullscreen, setFullscreen] = useState(false)
  const colors = BACKGROUNDS[background]

  useEffect(() => {
    const handleChange = () => setFullscreen(document.fullscreenElement === containerRef.current)
    document.addEventListener('fullscreenchange', handleChange)
    return () => document.removeEventListener('fullscreenchange', handleChange)
  }, [])

  const toggleFullscreen = () => {
    if (document.fullscreenElement) {
      document.exitFullscreen()
    } else {
      containerRef.current?.requestFullscreen()
    }
  }

  return (
    <div className="model-viewer" ref={containerRef}>
      <Canvas camera={{ position: [120, 100, 120], fov: 45 }}>
        <color attach="background" args={[colors.scene]} />
        <ambientLight intensity={0.5} />
        <directionalLight position={[120, 150, 100]} intensity={1.1} />
        <directionalLight position={[-100, 60, -80]} intensity={0.3} color="#7fa8d9" />
        <Suspense fallback={<Placeholder />}>
          {modelUrl ? (
            <StlModel url={modelUrl} wireframe={wireframe} onGeometryReady={setBounds} />
          ) : (
            <Placeholder />
          )}
        </Suspense>
        {showGrid && (
          <Grid
            position={[0, -0.01, 0]}
            args={[300, 300]}
            cellSize={10}
            cellThickness={0.5}
            cellColor={colors.gridCell}
            sectionSize={50}
            sectionThickness={1}
            sectionColor={colors.gridSection}
            fadeDistance={400}
            infiniteGrid
          />
        )}
        <OrbitControls ref={controlsRef} makeDefault />
        <CameraFitter bounds={bounds} controlsRef={controlsRef} />
        <ScreenshotHandler triggerRef={screenshotRef} />
      </Canvas>

      <ViewerToolbar
        visible={toolbarVisible}
        onToggleVisible={() => setToolbarVisible((v) => !v)}
        onZoomIn={() => dollyCamera(controlsRef, 0.8)}
        onZoomOut={() => dollyCamera(controlsRef, 1.25)}
        onReset={() => controlsRef.current?.reset()}
        wireframe={wireframe}
        onToggleWireframe={() => setWireframe((w) => !w)}
        background={background}
        onToggleBackground={() => setBackground((b) => (b === 'dark' ? 'light' : 'dark'))}
        showGrid={showGrid}
        onToggleGrid={() => setShowGrid((g) => !g)}
        showDimensions={showDimensions}
        onToggleDimensions={() => setShowDimensions((d) => !d)}
        onScreenshot={() => screenshotRef.current?.()}
        fullscreen={fullscreen}
        onToggleFullscreen={toggleFullscreen}
        downloadUrl={modelUrl}
      />

      {showDimensions && bounds?.size && (
        <p className={`viewer-hint${background === 'light' ? ' on-light' : ''}`}>
          X: {formatMm(bounds.size.x)} · Y: {formatMm(bounds.size.y)} · Z: {formatMm(bounds.size.z)}
        </p>
      )}

      {!modelUrl && (
        <p className={`viewer-hint${background === 'light' ? ' on-light' : ''}`}>Noch kein Modell generiert</p>
      )}
    </div>
  )
}

export default ModelViewer
