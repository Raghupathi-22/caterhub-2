import { Alert, Button, Card, CardContent, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { useEffect, useMemo, useState } from 'react'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import { workerApi } from '../api/workerApi'
import type { CatalogCategory } from '../types/models'

export function JoinCaterhubPage() {
  const [categories, setCategories] = useState<CatalogCategory[]>([])
  const [categoryId, setCategoryId] = useState('')
  const [roleId, setRoleId] = useState('')
  const [experienceYears, setExperienceYears] = useState('0')
  const [skills, setSkills] = useState('')
  const [preferredAreas, setPreferredAreas] = useState('')
  const [languages, setLanguages] = useState('')
  const [bio, setBio] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    catalogApi
      .getCategories()
      .then(setCategories)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load service catalog.')))
  }, [])

  const selectedCategory = useMemo(
    () => categories.find((category) => category.id === categoryId) ?? null,
    [categories, categoryId],
  )
  const selectedRole = useMemo(
    () => selectedCategory?.roles.find((role) => role.id === roleId) ?? null,
    [selectedCategory, roleId],
  )
  const roleSkills = selectedRole?.skills ?? []

  const submit = async () => {
    if (!selectedRole) return
    setError('')
    setSuccess('')
    try {
      await workerApi.createMyProfile({
        workerType: selectedRole.workerType,
        experienceYears: Number(experienceYears),
        skills,
        preferredAreas,
        languages,
        bio,
      })
      setSuccess('Worker profile submitted successfully. Your profile is under verification.')
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to submit worker profile.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Join CaterHub</Typography>
      <Typography color="text.secondary">Become a CaterHub partner and receive suitable opportunities.</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      {success ? <Alert severity="success">{success}</Alert> : null}
      <Card>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField fullWidth select label="Category" value={categoryId} onChange={(e) => { setCategoryId(e.target.value); setRoleId('') }}>
                <MenuItem value="">Select category</MenuItem>
                {categories.map((item) => <MenuItem key={item.id} value={item.id}>{item.name}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth select label="Role" value={roleId} onChange={(e) => setRoleId(e.target.value)} disabled={!selectedCategory}>
                <MenuItem value="">Select role</MenuItem>
                {selectedCategory?.roles.map((role) => <MenuItem key={role.id} value={role.id}>{role.title}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth type="number" label="Experience (years)" value={experienceYears} onChange={(e) => setExperienceYears(e.target.value)} />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Skills"
                value={skills}
                onChange={(e) => setSkills(e.target.value)}
                helperText={roleSkills.length > 0 ? `Suggested: ${roleSkills.join(', ')}` : 'Add relevant role skills'}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Preferred Areas" value={preferredAreas} onChange={(e) => setPreferredAreas(e.target.value)} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Languages" value={languages} onChange={(e) => setLanguages(e.target.value)} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Bio" value={bio} onChange={(e) => setBio(e.target.value)} multiline minRows={3} />
            </Grid>
          </Grid>
        </CardContent>
      </Card>
      <Button variant="contained" onClick={() => void submit()} disabled={!selectedRole}>
        Submit Worker Profile
      </Button>
    </Stack>
  )
}
